package org.elasticsearch.plugin.opengrok;

import static org.elasticsearch.common.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.OpenGrokAnalysisBinderProcessor;
import org.elasticsearch.index.analysis.OpenGrokTokenizerFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class OpenGrokTokenizerFactoryTests {
    private static TokenizerFactory factory;

    @BeforeClass
    public static void setUpClass() {
        // This code is based off of:
        // https://github.com/elasticsearch/elasticsearch-analysis-smartcn/blob/master/src/test/java/org/elasticsearch/index/analysis/SimpleSmartChineseAnalysisTests.java
        Index index = new Index("test");
        Injector parentInjector = new ModulesBuilder().add(
                new SettingsModule(EMPTY_SETTINGS),
                new EnvironmentModule(new Environment(EMPTY_SETTINGS)),
                new IndicesAnalysisModule()).createInjector();
        AnalysisModule analysisModule =
                new AnalysisModule(EMPTY_SETTINGS, parentInjector.getInstance(IndicesAnalysisService.class))
            .addProcessor(new OpenGrokAnalysisBinderProcessor());
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, EMPTY_SETTINGS),
                new IndexNameModule(index),
                analysisModule).createChildInjector(parentInjector);

        AnalysisService analysisService = injector.getInstance(AnalysisService.class);

        factory = analysisService.tokenizer("opengrok");
        Assert.assertTrue(factory instanceof OpenGrokTokenizerFactory);
    }

    @Test
    public void testC() {
        Tokenizer tokenizer = factory.create(new StringReader("a.c:/* my first function */int main(void) { printf(\"hello world\"); return 5 - 5; }"));
        ArrayList<String> tokens = readTokens(tokenizer);
        List<String> expected1 = Arrays.asList("my first function int main void printf hello world return 5 - 5".split(" "));
        Assert.assertEquals(expected1, tokens);
    }

    @Test
    public void testUnicode() {
        Tokenizer tokenizer = factory.create(new StringReader("a.c:abcdéfghi"));
        ArrayList<String> tokens = readTokens(tokenizer);
        List<String> expected1 = Arrays.asList("abcd fghi".split(" "));
        Assert.assertEquals(expected1, tokens);
    }

    private ArrayList<String> readTokens(Tokenizer tokenizer) {
        ArrayList<String> tokens = new ArrayList<>();
        CharTermAttribute attribute = tokenizer.addAttribute(CharTermAttribute.class);
        try {
            while (tokenizer.incrementToken()) {
                tokens.add(attribute.toString());
            }
        } catch (IOException e) {
            throw new Error("Impossible error", e);
        }
        return tokens;
    }
}
