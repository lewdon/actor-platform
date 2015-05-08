//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/rpc/RequestRemoveContact.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/rpc/RequestRemoveContact.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Request.h"
#include "java/io/IOException.h"

@interface ImActorModelApiRpcRequestRemoveContact () {
 @public
  jint uid_;
  jlong accessHash_;
}

@end

@implementation ImActorModelApiRpcRequestRemoveContact

+ (ImActorModelApiRpcRequestRemoveContact *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiRpcRequestRemoveContact_fromBytesWithByteArray_(data);
}

- (instancetype)initWithInt:(jint)uid
                   withLong:(jlong)accessHash {
  ImActorModelApiRpcRequestRemoveContact_initWithInt_withLong_(self, uid, accessHash);
  return self;
}

- (instancetype)init {
  ImActorModelApiRpcRequestRemoveContact_init(self);
  return self;
}

- (jint)getUid {
  return self->uid_;
}

- (jlong)getAccessHash {
  return self->accessHash_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->uid_ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
  self->accessHash_ = [values getLongWithInt:2];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:self->uid_];
  [writer writeLongWithInt:2 withLong:self->accessHash_];
}

- (NSString *)description {
  NSString *res = @"rpc RemoveContact{";
  res = JreStrcat("$$", res, JreStrcat("$I", @"uid=", self->uid_));
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return ImActorModelApiRpcRequestRemoveContact_HEADER;
}

@end

ImActorModelApiRpcRequestRemoveContact *ImActorModelApiRpcRequestRemoveContact_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiRpcRequestRemoveContact_initialize();
  return ((ImActorModelApiRpcRequestRemoveContact *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelApiRpcRequestRemoveContact_init(), data));
}

void ImActorModelApiRpcRequestRemoveContact_initWithInt_withLong_(ImActorModelApiRpcRequestRemoveContact *self, jint uid, jlong accessHash) {
  (void) ImActorModelNetworkParserRequest_init(self);
  self->uid_ = uid;
  self->accessHash_ = accessHash;
}

ImActorModelApiRpcRequestRemoveContact *new_ImActorModelApiRpcRequestRemoveContact_initWithInt_withLong_(jint uid, jlong accessHash) {
  ImActorModelApiRpcRequestRemoveContact *self = [ImActorModelApiRpcRequestRemoveContact alloc];
  ImActorModelApiRpcRequestRemoveContact_initWithInt_withLong_(self, uid, accessHash);
  return self;
}

void ImActorModelApiRpcRequestRemoveContact_init(ImActorModelApiRpcRequestRemoveContact *self) {
  (void) ImActorModelNetworkParserRequest_init(self);
}

ImActorModelApiRpcRequestRemoveContact *new_ImActorModelApiRpcRequestRemoveContact_init() {
  ImActorModelApiRpcRequestRemoveContact *self = [ImActorModelApiRpcRequestRemoveContact alloc];
  ImActorModelApiRpcRequestRemoveContact_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiRpcRequestRemoveContact)
