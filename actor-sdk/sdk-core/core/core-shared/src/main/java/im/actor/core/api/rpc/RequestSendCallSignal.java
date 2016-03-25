package im.actor.core.api.rpc;
/*
 *  Generated by the Actor API Scheme generator.  DO NOT EDIT!
 */

import im.actor.runtime.bser.*;
import im.actor.runtime.collections.*;

import static im.actor.runtime.bser.Utils.*;

import im.actor.core.network.parser.*;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import com.google.j2objc.annotations.ObjectiveCName;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import im.actor.core.api.*;

public class RequestSendCallSignal extends Request<ResponseVoid> {

    public static final int HEADER = 0xa27;

    public static RequestSendCallSignal fromBytes(byte[] data) throws IOException {
        return Bser.parse(new RequestSendCallSignal(), data);
    }

    private long callId;
    private byte[] content;

    public RequestSendCallSignal(long callId, @NotNull byte[] content) {
        this.callId = callId;
        this.content = content;
    }

    public RequestSendCallSignal() {

    }

    public long getCallId() {
        return this.callId;
    }

    @NotNull
    public byte[] getContent() {
        return this.content;
    }

    @Override
    public void parse(BserValues values) throws IOException {
        this.callId = values.getLong(1);
        this.content = values.getBytes(2);
    }

    @Override
    public void serialize(BserWriter writer) throws IOException {
        writer.writeLong(1, this.callId);
        if (this.content == null) {
            throw new IOException();
        }
        writer.writeBytes(2, this.content);
    }

    @Override
    public String toString() {
        String res = "rpc SendCallSignal{";
        res += "callId=" + this.callId;
        res += ", content=" + byteArrayToString(this.content);
        res += "}";
        return res;
    }

    @Override
    public int getHeaderKey() {
        return HEADER;
    }
}
