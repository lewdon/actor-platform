//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: /Users/ex3ndr/Develop/actor-model/library/actor-cocoa-base/build/java/im/actor/model/entity/PhoneBookPhone.java
//


#include "J2ObjC_source.h"
#include "im/actor/model/entity/PhoneBookPhone.h"

@interface AMPhoneBookPhone () {
 @public
  jlong id__;
  jlong number_;
}

@end

@implementation AMPhoneBookPhone

- (instancetype)initWithLong:(jlong)id_
                    withLong:(jlong)number {
  AMPhoneBookPhone_initWithLong_withLong_(self, id_, number);
  return self;
}

- (jlong)getId {
  return id__;
}

- (jlong)getNumber {
  return number_;
}

@end

void AMPhoneBookPhone_initWithLong_withLong_(AMPhoneBookPhone *self, jlong id_, jlong number) {
  (void) NSObject_init(self);
  self->id__ = id_;
  self->number_ = number;
}

AMPhoneBookPhone *new_AMPhoneBookPhone_initWithLong_withLong_(jlong id_, jlong number) {
  AMPhoneBookPhone *self = [AMPhoneBookPhone alloc];
  AMPhoneBookPhone_initWithLong_withLong_(self, id_, number);
  return self;
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(AMPhoneBookPhone)
