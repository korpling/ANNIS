package org.corpus_tools.annis.gui.hibernate.type;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.net.URI;
import java.sql.Clob;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;

public class URITypeDescriptor extends AbstractTypeDescriptor<URI>
{

  private static final long serialVersionUID = 4214996442852980256L;
  public static final URITypeDescriptor INSTANCE = new URITypeDescriptor();

  public URITypeDescriptor() {
    super(URI.class);
  }

  @Override
  public URI fromString(String string) {
    try {
      return URI.create(string);
    } catch (IllegalArgumentException e) {
      throw new HibernateException("Unable to convert string [" + string + "] to URI", e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <X> X unwrap(URI value, Class<X> type, WrapperOptions options) {
    if (value == null) {
      return null;
    }
    if (String.class.isAssignableFrom(type)) {
      return (X) toString(value);
    } else if(CharacterStream.class.isAssignableFrom(type)) {
      return (X) new CharacterStreamImpl(toString(value));
    }
    throw unknownUnwrap(type);
}

  @Override
  public <X> URI wrap(X value, WrapperOptions options) {
    if (value == null) {
      return null;
    }
    if (String.class.isInstance(value)) {
      return fromString((String) value);
    } else if (CharacterStream.class.isInstance(value)) {
      return fromString(((CharacterStream) value).asString());
    } else if (Clob.class.isInstance(value)) {
      Clob c = (Clob) value;
      try {
        return fromString(CharStreams.toString(c.getCharacterStream()));
      } catch (IOException | SQLException e) {
        throw new HibernateException("Unable to convert Clob to URI", e);
      }
    }
    throw unknownWrap(value.getClass());
  }
}
