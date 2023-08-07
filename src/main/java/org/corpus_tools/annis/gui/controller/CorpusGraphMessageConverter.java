package org.corpus_tools.annis.gui.controller;

import java.io.File;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import org.corpus_tools.annis.gui.graphml.CorpusGraphMapper;
import org.corpus_tools.salt.common.SCorpusGraph;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpInputMessage;
import org.springframework.http.codec.HttpMessageReader;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CorpusGraphMessageConverter implements HttpMessageReader<SCorpusGraph> {


  @Override
  public List<MediaType> getReadableMediaTypes() {
    // TODO Auto-generated method stub
    return Arrays.asList(MediaType.APPLICATION_XML);
  }

  @Override
  public boolean canRead(ResolvableType elementType, MediaType mediaType) {
    return elementType.isAssignableFrom(SCorpusGraph.class)
        && MediaType.APPLICATION_XML.equals(mediaType);
  }

  @Override
  public Flux<SCorpusGraph> read(ResolvableType elementType, ReactiveHttpInputMessage message,
      Map<String, Object> hints) {
    // We can only read a single SDocumentGraph object from a request
    return readMono(elementType, message, hints).flux();
  }

  @Override
  public Mono<SCorpusGraph> readMono(ResolvableType elementType, ReactiveHttpInputMessage message,
      Map<String, Object> hints) {
    try {
      File graphML = File.createTempFile("annis-subgraph-", ".salt");
      WritableByteChannel fileChannel =
          Files.newByteChannel(graphML.toPath(), StandardOpenOption.WRITE);
      return DataBufferUtils.write(message.getBody(), fileChannel).map(DataBufferUtils::release)
          .then(Mono.just(graphML)).flatMap(f -> {
            try {
              SCorpusGraph result = CorpusGraphMapper.map(f);
              Files.deleteIfExists(f.toPath());
              return Mono.just(result);
            } catch (XMLStreamException | IOException ex) {
              return Mono.error(ex);
            }
          });

    } catch (IOException ex) {
      return Mono.error(ex);
    }
  }

}
