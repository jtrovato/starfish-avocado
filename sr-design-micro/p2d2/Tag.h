class Tag{
  byte type;
  byte data1;
  byte data2;
public:
  byte getData1();
public:
  byte getData2();
public:
  byte getType();
public: 
  void setValues(byte x, byte y);
public: 
  void setValues(byte x, byte y, byte z);
};


byte Tag::getData1(){
  return data1;
}

byte Tag::getData2(){
  return data2;
}

byte Tag::getType(){
  return type;
}

void Tag::setValues (byte x, byte y) {
  type = x;
  data1 = y;
}

void Tag::setValues (byte x, byte y, byte z) {
  type = x;
  data1 = y;
  data2 = z;
}




