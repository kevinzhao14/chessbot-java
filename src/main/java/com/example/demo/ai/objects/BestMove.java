package com.example.demo.ai.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class BestMove {
    Pos from;
    Pos to;
    char promote;
    long nodes;

    public BestMove(Pos from, Pos to, char promote) {
        this.from = from;
        this.to = to;
        this.promote = promote;
    }

    public void setFrom(Pos from) {
        this.from = from;
    }

    public void setTo(Pos to) {
        this.to = to;
    }

    public void setPromote(char promote) {
        this.promote = promote;
    }

    public long getNodes() {
        return nodes;
    }

    public void setNodes(long nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        String str = "{";
        str += "\"from\":";
        if (from != null) {
            str += "[" + from.x() + "," + from.y() + "]";
        } else {
            str += "null";
        }
        str += ",\"to\":";
        if (to != null) {
            str += "[" + to.x() + "," + to.y() + "]";
        } else {
            str += "null";
        }
        str += ",\"options\":{";
        if (promote != 0) {
            str += "\"promote\":\"" + promote + "\"";
        }
        str += "}}";
        return str;
    }

    public static class BestMoveSerializer extends StdSerializer<BestMove> {

        public BestMoveSerializer() {
            this(null);
        }

        public BestMoveSerializer(Class<BestMove> t) {
            super(t);
        }

        @Override
        public void serialize(BestMove value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeArrayFieldStart("from");
            gen.writeArray(value.from.toArray(), 0, 2);
            gen.writeArrayFieldStart("to");
            gen.writeArray(value.to.toArray(), 0, 2);
            if (value.promote != 0) {
                gen.writeStringField("promote", String.valueOf(value.promote));
            }
            gen.writeEndObject();
        }
    }
}
