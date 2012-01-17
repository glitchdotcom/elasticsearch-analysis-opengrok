package org.elasticsearch.plugin.analysis.smartcn;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;

public class AnalysisOpenGrokPlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "analysis-opengrok";
    }

    @Override
    public String description() {
        return "Smart Chinese analysis support";
    }

    @Override
    public void processModule(Module module) {
        if (module instanceof AnalysisModule) {
            AnalysisModule analysisModule = (AnalysisModule) module;
        }
    }
}