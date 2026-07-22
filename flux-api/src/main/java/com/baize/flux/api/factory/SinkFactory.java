package com.baize.flux.api.factory; import com.baize.flux.api.sink.Sink; import java.util.Map; public interface SinkFactory extends Factory { Sink create(Map<String,String> options); }
