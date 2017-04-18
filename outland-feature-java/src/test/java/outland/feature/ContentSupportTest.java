package outland.feature;

import com.google.gson.Gson;
import org.junit.Test;
import outland.feature.proto.Feature;

import static org.junit.Assert.assertEquals;

public class ContentSupportTest {

  @Test
  public void testToJsonBytes() throws Exception {

    final Feature in = TestSupport.loadFeature("json/feature-1.json");
    final ContentSupport cs = new ContentSupport();
    final byte[] bytes = cs.toJsonBytes(in);
    final String raw = new String(bytes, "UTF-8");
    final Feature feature = new FeatureSupport().toFeature(raw);
    assertEquals(in, feature);
  }

  @Test
  public void testFromJson() {

    final Problem in = Problem.localProblem("title", "detail");
    final String raw = new Gson().toJson(in);
    final ContentSupport cs = new ContentSupport();
    final Problem out = cs.fromJson(raw, Problem.class);
    assertEquals("title", out.title());
    assertEquals("detail", out.detail().get());
    assertEquals(in, out);
  }

}