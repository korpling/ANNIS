/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package annis.ql.parser;

import com.google.common.base.Preconditions;
import java.util.List;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ListTokenSource implements TokenSource
{
  private final List<Token> token;
  private TokenFactory factory;
  private final CommonToken eofToken = new CommonToken(Lexer.EOF, "");

  public ListTokenSource(List<Token> token)
  {
    this.token = token;
    Preconditions.checkNotNull(token);
    Preconditions.checkArgument(!token.isEmpty(), "Internal token list must not be empty");
    
    for(Token t : token)
    {
      if(t.getTokenSource() != null)
      {
        this.factory = t.getTokenSource().getTokenFactory();
        break;
      }
    }
    Preconditions.checkNotNull(this.factory, "Internal token list needs a valid TokenSource");
    
    Token lastToken = token.get(token.size()-1);
    eofToken.setLine(lastToken.getLine());
    eofToken.setCharPositionInLine(lastToken.getCharPositionInLine());
  }

  @Override
  public Token nextToken()
  {
    if(token.isEmpty())
    {
      return eofToken;
    }
    else
    {
      return token.remove(0);
    }
  }

  @Override
  public int getLine()
  {
    if(token.isEmpty())
    {
      return eofToken.getLine();
    }
    else
    {
      return token.get(0).getLine();
    }
  }

  @Override
  public int getCharPositionInLine()
  {
    if(token.isEmpty())
    {
      return eofToken.getCharPositionInLine();
    }
    else
    {
      return token.get(0).getCharPositionInLine();
    }
  }

  @Override
  public CharStream getInputStream()
  {
    if(token.isEmpty())
    {
      return eofToken.getInputStream();
    }
    else
    {
      return token.get(0).getInputStream();
    }
  }

  @Override
  public String getSourceName()
  {
    if(token.isEmpty())
    {
      return eofToken.getInputStream().getSourceName();
    }
    else
    {
      return token.get(0).getInputStream().getSourceName();
    }
  }

  @Override
  public TokenFactory getTokenFactory()
  {
    return factory;
  }

  @Override
  public void setTokenFactory(TokenFactory factory)
  {
    this.factory = factory;
  }
  
  
}
