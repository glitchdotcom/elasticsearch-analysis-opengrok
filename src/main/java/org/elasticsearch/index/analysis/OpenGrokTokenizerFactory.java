package org.elasticsearch.index.analysis;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.pattern.PatternTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.opensolaris.opengrok.analysis.AnalyzerGuru;

public class OpenGrokTokenizerFactory extends AbstractTokenizerFactory {
    private static Pattern splitOnSpace = Pattern.compile(" +");

    @Inject
    public OpenGrokTokenizerFactory(Index index, Settings indexSettings,
            String name, Settings settings) {
        super(index, indexSettings, name, settings);
    }

    @Override
    public Tokenizer create(Reader reader) {
        // The one wrinkle is that we need the user to specify the filename
        // in the form of {filename}:{contents}.
        String text = new Scanner(reader).useDelimiter("\\A").next();
        String[] parts = text.split(":", 2);
        String filename = parts[0];
        String contents = parts[1];

        try (
             Analyzer analyzer = AnalyzerGuru.find(filename).getAnalyzer();
             TokenStream stream = analyzer.tokenStream("full", new StringReader(contents))
        ) {
            // Tokenizer is an abstract class that generates tokens from a
            // Reader but all I have is a TokenStream. Conscripting
            // PatternTokenizer *seems* like the the easiest way to do
            // things. Since we know text is at most ~10  KB, there's no
            // problem if we do everything in memory.
            String tokens = convertTokenStream(stream);
            return new PatternTokenizer(new StringReader(tokens), splitOnSpace, -1);

        } catch (IOException e) {
            throw new Error("Impossible", e);
        }
    }

    private String convertTokenStream(TokenStream stream) {
        CharTermAttribute charTermAttribute = stream.getAttribute(CharTermAttribute.class);
        StringBuilder builder = new StringBuilder();
        try {
            while (stream.incrementToken()) {
                String term = charTermAttribute.toString();
                builder.append(term);
                builder.append(" ");
            }
            return builder.toString();
        } catch (IOException e) {
            throw new Error("Impossible", e);
        }
    }
}
