package org.corpus_tools.annis.gui.hibernate.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.sql.Clob;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.CharacterStream;
import org.junit.jupiter.api.Test;

class URITypeDescriptorTest {

  public final static String TEST_URI_STR = "/abc";
  public final static URI TEST_URI = URI.create("/abc");

  @Test
  void testFromString() {
    URITypeDescriptor descriptor = URITypeDescriptor.INSTANCE;
    
    assertEquals(TEST_URI, descriptor.fromString(TEST_URI_STR));
    assertThrows(HibernateException.class, () -> descriptor.fromString("http:"));
  }

  @Test
  void testUnwrap() {
    URITypeDescriptor descriptor = URITypeDescriptor.INSTANCE;

    assertNull(descriptor.unwrap(null, CharacterStream.class, null));

    Object cs = descriptor.unwrap(TEST_URI, CharacterStream.class, null);
    assertTrue(cs instanceof CharacterStream);
    assertEquals(TEST_URI_STR, ((CharacterStream) cs).asString());

    assertThrows(HibernateException.class, () -> descriptor.unwrap(TEST_URI, String.class, null));
  }



  @Test
  void testWrap() throws SQLException, IOException {
    URITypeDescriptor descriptor = URITypeDescriptor.INSTANCE;

    assertNull(descriptor.wrap(null, null));

    Clob clob = mock(Clob.class);

    when(clob.getCharacterStream()).thenReturn(new StringReader(TEST_URI_STR));
    assertEquals(TEST_URI, descriptor.wrap(clob, null));

    assertThrows(HibernateException.class, () -> descriptor.wrap(TEST_URI_STR, null));

    Clob clob2 = mock(Clob.class);
    when(clob2.getCharacterStream()).thenThrow(SQLException.class);
    assertThrows(HibernateException.class, () -> descriptor.wrap(clob2, null));
    
    assertThrows(HibernateException.class, () -> descriptor.wrap(TEST_URI_STR, null));
  }
}
