package org.elasticsearch.index.analysis;

public class OpenGrokAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {
    @Override
    public void processTokenizers(TokenizersBindings bindings) {
        bindings.processTokenizer("opengrok", OpenGrokTokenizerFactory.class);
    }
}
