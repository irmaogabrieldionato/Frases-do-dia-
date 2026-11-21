"""Offline artwork for Android, Android TV, iOS and Windows (no third-party dependencies)."""
import struct, zlib
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
def png(w,h,banner=False,rgb=False):
    raw=bytearray()
    for y in range(h):
        raw.append(0)
        for x in range(w):
            u,v=(x+.5)/w,(y+.5)/h
            a=(u+v)/2
            r,g,b=int(24+16*a),int(31+22*a),int(56+34*a)
            cx,cy,s=(.30,.5,.62) if banner else (.5,.5,.95)
            xx,yy=(u-cx)/s,(v-cy)/s
            if xx*xx+yy*yy<.183:r,g,b=49,64,105
            head=((xx+.13)/.16)**2+((yy-.21)/.095)**2<1
            stem=.006<xx<.067 and -.29<yy<.19
            flag=-.29<yy<-.16 and .03<xx<.23 and yy>-.29+(xx-.03)*.5
            if head or stem or flag:r,g,b=251,213,150
            if not banner:
                for i,col in enumerate(((251,145,139),(255,190,118),(249,219,126),(158,227,181),(129,207,248),(193,166,247),(243,169,219))):
                    if (u-(.18+i*.106))**2+(v-.85)**2<.00049:r,g,b=col;break
            raw.extend((r,g,b) if rgb else (r,g,b,255))
    def chunk(tag,data):
        return struct.pack(">I",len(data))+tag+data+struct.pack(">I",zlib.crc32(tag+data)&0xffffffff)
    color_type=2 if rgb else 6
    return b"\x89PNG\r\n\x1a\n"+chunk(b"IHDR",struct.pack(">IIBBBBB",w,h,8,color_type,0,0,0))+chunk(b"IDAT",zlib.compress(raw,9))+chunk(b"IEND",b"")
def write(path,data):
    dest=ROOT/path
    dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_bytes(data)
    print("Generated",path)
if __name__=="__main__":
    write("native/android/app/src/main/res/mipmap-nodpi/ic_launcher.png",png(512,512))
    write("native/android/app/src/main/res/drawable-nodpi/tv_banner.png",png(320,180,True))
    write("native/ios/SevenNote/Assets.xcassets/AppIcon.appiconset/icon-1024.png",png(1024,1024,rgb=True))
    sizes=(16,32,48,64,128,256)
    data=[png(s,s) for s in sizes]
    result=bytearray(struct.pack("<HHH",0,1,len(sizes)))
    offset=6+16*len(sizes)
    for size,img in zip(sizes,data):
        result.extend(struct.pack("<BBBBHHII",size%256,size%256,0,0,1,32,len(img),offset))
        offset+=len(img)
    write("native/windows/build/icon.ico",bytes(result)+b"".join(data))
