package org.corpus_tools.annis.gui.hibernate.type;

import java.net.URI;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;

public class URIType extends AbstractSingleColumnStandardBasicType<URI> {

  private static final long serialVersionUID = -4460284202282713280L;

  public URIType() {
    super(ClobTypeDescriptor.DEFAULT, URITypeDescriptor.INSTANCE);
  }

  @Override
  public String getName() {
    return "uri";
  }
}
