package outland.feature;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.CompactionStyle;
import org.rocksdb.CompressionType;
import org.rocksdb.FlushOptions;
import org.rocksdb.Options;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.rocksdb.WriteOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import outland.feature.proto.Feature;
import outland.feature.proto.FeatureCollection;

class FeatureStoreRocksDb implements FeatureStoreLocal, MeterTimer {

  private static final Logger logger = LoggerFactory.getLogger("FeatureClient");

  private static final String METRICS_PATH = "outland.feature.client.store.rocks";

  private static final long WRITE_BUFFER_SIZE = 4 * 1024 * 1024L;
  private static final long BLOCK_CACHE_SIZE = 10 * 1024 * 1024L;
  private static final long BLOCK_SIZE = 2048L;
  private static final int MAX_WRITE_BUFFER_NUMBER = 4;
  private static final boolean CREATE_IF_MISSING = true;
  private static final boolean ERROR_IF_EXISTS = false;
  private static final CompressionType COMPRESSION_TYPE = CompressionType.SNAPPY_COMPRESSION;
  private static final CompactionStyle COMPACTION_STYLE = CompactionStyle.UNIVERSAL;
  private static final boolean DISABLE_WAL = true;
  private static final boolean WAIT_FOR_FLUSH = true;

  private final Options options;
  private final WriteOptions writeOptions;
  private final FlushOptions flushOptions;
  private final ReadOptions readOptions;

  private final Timer putTimer;
  private final Meter putMeter;
  private final Timer loadAllTimer;
  private final Meter loadAllMeter;
  private final Timer findAllTimer;
  private final Meter findAllMeter;

  private File rocksDir;
  private RocksDB rocks;

  FeatureStoreRocksDb(MetricsContext metricsContext, String dbPath) {
    this(metricsContext, new File(dbPath));
  }

  FeatureStoreRocksDb(MetricsContext metricsContext) {
    this(metricsContext, new File("/tmp", "outland"));
  }

  FeatureStoreRocksDb(MetricsContext metricsContext, File dbPath) {

    MetricRegistry metrics = metricsContext.metrics();
    String context = metricsContext.context();

    putTimer = metrics.timer(MetricRegistry.name(
        context + "." + METRICS_PATH, "putTimer"));
    putMeter = metrics.meter(MetricRegistry.name(
        context + "." + METRICS_PATH, "putMeter"));
    this.loadAllTimer = metrics.timer(MetricRegistry.name(
        context + "." + METRICS_PATH, "loadAllTimer"));
    this.loadAllMeter = metrics.meter(MetricRegistry.name(
        context + "." + METRICS_PATH, "loadAllMeter"));

    this.findAllTimer = metrics.timer(MetricRegistry.name(
        context + "." + METRICS_PATH, "findAllTimer"));
    this.findAllMeter = metrics.meter(MetricRegistry.name(
        context + "." + METRICS_PATH, "findAllMeter"));

    BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
    tableConfig.setBlockCacheSize(BLOCK_CACHE_SIZE);
    tableConfig.setBlockSize(BLOCK_SIZE);

    options = new Options();
    options.setTableFormatConfig(tableConfig);
    options.setWriteBufferSize(WRITE_BUFFER_SIZE);
    options.setCompressionType(COMPRESSION_TYPE);
    options.setCompactionStyle(COMPACTION_STYLE);
    options.setMaxWriteBufferNumber(MAX_WRITE_BUFFER_NUMBER);
    options.setCreateIfMissing(CREATE_IF_MISSING);
    options.setErrorIfExists(ERROR_IF_EXISTS);
    writeOptions = new WriteOptions();
    writeOptions.setDisableWAL(DISABLE_WAL);
    writeOptions.setSync(true);
    readOptions = new ReadOptions();
    readOptions.setVerifyChecksums(true);
    readOptions.setFillCache(true);
    flushOptions = new FlushOptions();
    flushOptions.setWaitForFlush(WAIT_FOR_FLUSH);

    final File parent = new File("/tmp", "outland");
    rocksDir = new File(dbPath, "feature-store");
    //noinspection ResultOfMethodCallIgnored
    rocksDir.getParentFile().mkdirs();
    rocks = initializeRocksDb(); // todo: move this out?
  }

  @SuppressWarnings("WeakerAccess") @VisibleForTesting
  RocksDB initializeRocksDb() {
    try {
      final String absolutePath = rocksDir.getAbsolutePath();
      logger.info("op=initialise_rocks, path={}", absolutePath);
      return RocksDB.open(options, absolutePath);
    } catch (RocksDBException e) {
      logger.warn(
          "op=create_rocks, result=create_rocks_failed, msg='could not create a rocks db client storage actions will be skipped'");
      logger.error(e.getMessage());
    }
    return null;
  }

  @Override public Void put(Feature feature) {
    if (rocks == null) {
      logger.warn("op=put storage=rocks, result=skip, msg='no rocks db client available'");
      return null;
    }

    final String storageKey = FeatureStoreKeys.storageKey(feature.getNamespace(), feature.getKey());

    logger.info("op=put, storage=rocks, namespace={}, feature_key={}, storage_key={}",
        feature.getNamespace(), feature.getKey(), storageKey);

    return metric(putTimer, putMeter, () -> putInner(storageKey, feature));
  }

  @Override public FeatureCollection loadAll() throws FeatureException {
    if (rocks == null) {
      logger.warn("op=loadAll storage=rocks, result=skip, msg='no rocks db client available'");
      return null;
    }

    logger.info("op=loadAll, storage=rocks");

    return metric(loadAllTimer, loadAllMeter, this::loadAllInner);
  }

  @Override public FeatureCollection findAll(String namespace) {
    if (rocks == null) {
      logger.warn("op=findAll, storage=rocks, action=skip, msg='no rocks db client available'");
      return FeatureCollection.newBuilder().build();
    }

    logger.info("op=findAll, storage=rocks, namespace={}", namespace);

    return metric(findAllTimer, findAllMeter, () -> this.findAllInner(namespace));
  }

  @Override public void close() {
    logger.warn("op=close, storage=rocks");

    if (rocks == null) {
      logger.warn("op=close, storage=rocks, result=skip, msg='no rocks db client available'");
      return;
    }

    try {
      rocks.flush(flushOptions);
      flushOptions.dispose();

      options.dispose();
      writeOptions.dispose();
      readOptions.dispose();
      flushOptions.dispose();
      rocks.close();

      logger.info("op=close, storage=rocks, result=ok");
    } catch (RocksDBException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private FeatureCollection findAllInner(String namespace) {
    final FeatureCollection.Builder builder = FeatureCollection.newBuilder();

    try {
      final byte[] prefix =
          FeatureStoreKeys.storageKeyPrefix(namespace).getBytes(StandardCharsets.UTF_8);

      String prefixString = new String(prefix);

      final RocksIterator iterator = rocks.newIterator();
      for (iterator.seek(prefix);
          iterator.isValid();
          iterator.next()) {

        final String key = new String(iterator.key());
        /*
         iterator.seek(prefix) is a start point not a filter, we need to check our keys
          */
        if(key.startsWith(prefixString)) {
          final Feature feature = Feature.newBuilder().mergeFrom(iterator.value()).build();
          logger.info("op=findAll, storage=rocks, namespace={}, feature_key={}",
              namespace, feature.getKey());
          builder.addItems(feature);
        }

      }
    } catch (Exception e) {
      throw new FeatureException(
          Problem.localProblem("error finding from rocksdb namespace=" + namespace, e.getMessage()), e);
    }

    return builder.build();
  }

  private FeatureCollection loadAllInner() {
    final FeatureCollection.Builder builder = FeatureCollection.newBuilder();

    try {
      final RocksIterator iterator = rocks.newIterator();
      for (iterator.seekToFirst();
          iterator.isValid();
          iterator.next()) {
        final Feature feature = Feature.newBuilder().mergeFrom(iterator.value()).build();
        logger.info("op=loadAll, storage=rocks, namespace={}, feature_key={}",
            feature.getNamespace(), feature.getKey());
        builder.addItems(feature);
      }
    } catch (Exception e) {
      throw new FeatureException(
          Problem.localProblem("error loading all from rocksdb", e.getMessage()), e);
    }

    return builder.build();
  }

  private Void putInner(String storageKey, Feature feature) {
    try {
      final byte[] bytes = storageKey.getBytes(StandardCharsets.UTF_8);
      final byte[] value = feature.toByteArray();
      rocks.put(writeOptions, bytes, value);
    } catch (RocksDBException e) {
      logger.error(e.getMessage(), e);
    }

    return null;
  }
}
