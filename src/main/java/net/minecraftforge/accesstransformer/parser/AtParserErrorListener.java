package net.minecraftforge.accesstransformer.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class AtParserErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line, final int charPositionInLine, final String msg,
                            final RecognitionException e) {
        throw new RuntimeException("");
    }

}
