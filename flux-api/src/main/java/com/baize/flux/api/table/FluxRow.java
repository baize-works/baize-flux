package com.baize.flux.api.table;
import java.util.*;
public record FluxRow(List<Object> values) { public FluxRow { values=List.copyOf(values); } }
