package outland.feature.server.protobuf;

import com.google.gson.stream.MalformedJsonException;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import outland.feature.server.Problem;
import outland.feature.server.ServiceException;

@Provider
@Consumes(
    {
        Protobuf3MediaType.APPLICATION_PROTOBUF,
        Protobuf3MediaType.APPLICATION_PROTOBUF_JSON,
        MediaType.APPLICATION_JSON
    }
)
@Produces(
    {
        Protobuf3MediaType.APPLICATION_PROTOBUF,
        Protobuf3MediaType.APPLICATION_PROTOBUF_TEXT,
        Protobuf3MediaType.APPLICATION_PROTOBUF_JSON,
        MediaType.APPLICATION_JSON
    }
)
public class Protobuf3MessageBodyProvider
    implements MessageBodyReader<Message>, MessageBodyWriter<Message> {

  @Override
  public boolean isReadable(final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType
  ) {
    return Message.class.isAssignableFrom(type);
  }

  @Override
  public Message readFrom(final Class<Message> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType,
      final MultivaluedMap<String, String> httpHeaders,
      final InputStream entityStream
  ) throws IOException {

    try {

      final Method newBuilder = type.getMethod("newBuilder");
      final Message.Builder builder = (Message.Builder) newBuilder.invoke(type);

      if (mediaType.getSubtype().contains("json")) {
        BufferedReader br =
            new BufferedReader(new InputStreamReader(entityStream, StandardCharsets.UTF_8));
        JsonFormat.parser().merge(br, builder);
        return builder.build();
      }
      return builder.mergeFrom(entityStream).build();
    } catch (MalformedJsonException | InvalidProtocolBufferException e) {
      throw new ServiceException(Problem.clientProblem("request_entity_invalid",
          "The submitted content was invalid: " + e.getMessage(), 422), e);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new ServiceException(Problem.clientProblem("request_entity_failure",
          "A request handler could not be loaded: " + e.getMessage(), 500), e);
    }
  }

  @Override
  public boolean isWriteable(final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType
  ) {
    return Message.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(final Message m,
      final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType
  ) {
    return -1; // as per jax-rs 2 guidance, this value is ignored by runtimes.
  }

  @Override
  public void writeTo(final Message m,
      final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType,
      final MultivaluedMap<String, Object> httpHeaders,
      final OutputStream entityStream
  ) throws IOException {

    if (mediaType.getSubtype().contains("protobuf+text")) {
      entityStream.write(m.toString().getBytes(StandardCharsets.UTF_8));
    } else if (mediaType.getSubtype().contains("json")) {
      try {
        entityStream.write(Protobuf3Support.toJsonString(m).getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      entityStream.write(m.toByteArray());
    }
  }
}
