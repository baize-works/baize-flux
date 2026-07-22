package com.baize.flux.api.factory; import com.baize.flux.api.source.Source; import java.util.Map; public interface SourceFactory extends Factory { Source create(Map<String,String> options); }
