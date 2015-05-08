//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/api/updates/UpdateContactRegistered.java
//


#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "im/actor/model/api/updates/UpdateContactRegistered.h"
#include "im/actor/model/droidkit/bser/Bser.h"
#include "im/actor/model/droidkit/bser/BserObject.h"
#include "im/actor/model/droidkit/bser/BserValues.h"
#include "im/actor/model/droidkit/bser/BserWriter.h"
#include "im/actor/model/network/parser/Update.h"
#include "java/io/IOException.h"

@interface ImActorModelApiUpdatesUpdateContactRegistered () {
 @public
  jint uid_;
  jboolean isSilent__;
  jlong date_;
  jlong rid_;
}

@end

@implementation ImActorModelApiUpdatesUpdateContactRegistered

+ (ImActorModelApiUpdatesUpdateContactRegistered *)fromBytesWithByteArray:(IOSByteArray *)data {
  return ImActorModelApiUpdatesUpdateContactRegistered_fromBytesWithByteArray_(data);
}

- (instancetype)initWithInt:(jint)uid
                withBoolean:(jboolean)isSilent
                   withLong:(jlong)date
                   withLong:(jlong)rid {
  ImActorModelApiUpdatesUpdateContactRegistered_initWithInt_withBoolean_withLong_withLong_(self, uid, isSilent, date, rid);
  return self;
}

- (instancetype)init {
  ImActorModelApiUpdatesUpdateContactRegistered_init(self);
  return self;
}

- (jint)getUid {
  return self->uid_;
}

- (jboolean)isSilent {
  return self->isSilent__;
}

- (jlong)getDate {
  return self->date_;
}

- (jlong)getRid {
  return self->rid_;
}

- (void)parseWithBSBserValues:(BSBserValues *)values {
  self->uid_ = [((BSBserValues *) nil_chk(values)) getIntWithInt:1];
  self->isSilent__ = [values getBoolWithInt:2];
  self->date_ = [values getLongWithInt:3];
  self->rid_ = [values getLongWithInt:4];
}

- (void)serializeWithBSBserWriter:(BSBserWriter *)writer {
  [((BSBserWriter *) nil_chk(writer)) writeIntWithInt:1 withInt:self->uid_];
  [writer writeBoolWithInt:2 withBoolean:self->isSilent__];
  [writer writeLongWithInt:3 withLong:self->date_];
  [writer writeLongWithInt:4 withLong:self->rid_];
}

- (NSString *)description {
  NSString *res = @"update ContactRegistered{";
  res = JreStrcat("$$", res, JreStrcat("$I", @"uid=", self->uid_));
  res = JreStrcat("$$", res, JreStrcat("$Z", @", isSilent=", self->isSilent__));
  res = JreStrcat("$$", res, JreStrcat("$J", @", date=", self->date_));
  res = JreStrcat("$C", res, '}');
  return res;
}

- (jint)getHeaderKey {
  return ImActorModelApiUpdatesUpdateContactRegistered_HEADER;
}

@end

ImActorModelApiUpdatesUpdateContactRegistered *ImActorModelApiUpdatesUpdateContactRegistered_fromBytesWithByteArray_(IOSByteArray *data) {
  ImActorModelApiUpdatesUpdateContactRegistered_initialize();
  return ((ImActorModelApiUpdatesUpdateContactRegistered *) BSBser_parseWithBSBserObject_withByteArray_(new_ImActorModelApiUpdatesUpdateContactRegistered_init(), data));
}

void ImActorModelApiUpdatesUpdateContactRegistered_initWithInt_withBoolean_withLong_withLong_(ImActorModelApiUpdatesUpdateContactRegistered *self, jint uid, jboolean isSilent, jlong date, jlong rid) {
  (void) ImActorModelNetworkParserUpdate_init(self);
  self->uid_ = uid;
  self->isSilent__ = isSilent;
  self->date_ = date;
  self->rid_ = rid;
}

ImActorModelApiUpdatesUpdateContactRegistered *new_ImActorModelApiUpdatesUpdateContactRegistered_initWithInt_withBoolean_withLong_withLong_(jint uid, jboolean isSilent, jlong date, jlong rid) {
  ImActorModelApiUpdatesUpdateContactRegistered *self = [ImActorModelApiUpdatesUpdateContactRegistered alloc];
  ImActorModelApiUpdatesUpdateContactRegistered_initWithInt_withBoolean_withLong_withLong_(self, uid, isSilent, date, rid);
  return self;
}

void ImActorModelApiUpdatesUpdateContactRegistered_init(ImActorModelApiUpdatesUpdateContactRegistered *self) {
  (void) ImActorModelNetworkParserUpdate_init(self);
}

ImActorModelApiUpdatesUpdateContactRegistered *new_ImActorModelApiUpdatesUpdateContactRegistered_init() {
  ImActorModelApiUpdatesUpdateContactRegistered *self = [ImActorModelApiUpdatesUpdateContactRegistered alloc];
  ImActorModelApiUpdatesUpdateContactRegistered_init(self);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ImActorModelApiUpdatesUpdateContactRegistered)
