package com.eddy.model;

import java.util.Objects;
import java.util.Set;

import static com.eddy.model.RateLimitConstants.DEFAULT_DURATION;
import static com.eddy.model.RateLimitConstants.DEFAULT_LIMIT;
import static com.eddy.model.RateLimitConstants.DEFAULT_PRECISION;

public class RequestLimitRule {
    private int duration;
    private long limit;
    private int precision;
    private String name;
    private Set<String> keys;

    public RequestLimitRule() {
        this.duration = DEFAULT_DURATION;
        this.precision = DEFAULT_PRECISION;
        this.limit = DEFAULT_LIMIT;
    }

    public RequestLimitRule(Set<String> keys) {
        this.keys = keys;
        this.duration = DEFAULT_DURATION;
        this.precision = DEFAULT_PRECISION;
        this.limit = DEFAULT_LIMIT;
    }

    public RequestLimitRule(int duration, long limit, Set<String> keys) {
        this.duration = duration;
        this.precision = duration;
        this.limit = limit;
        this.keys = keys;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getKeys() {
        return keys;
    }

    public void setKeys(Set<String> keys) {
        this.keys = keys;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        RequestLimitRule that = (RequestLimitRule) o;
        return duration == that.duration && limit == that.limit && precision == that.precision && Objects.equals(name, that.name) && Objects.equals(keys, that.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(duration, limit, precision, name, keys);
    }

    @Override
    public String toString() {
        return "RequestLimitRule{" +
                "duration=" + duration +
                ", limit=" + limit +
                ", precision=" + precision +
                ", name='" + name + '\'' +
                ", keys=" + keys +
                '}';
    }
}
