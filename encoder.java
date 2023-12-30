import java.nio.charset.StandardCharsets;

class HelloWorld {
    public static void main(String[] args) {
        //byte[] in  = new byte[] {-33,0,5,2,1,17,0,0};// call end
        //byte[] in  = new byte[] {-33,0,15,2,1,18,0,20,1,0,0,'1','2','3',':','A','B','C'};// sms

        String sender_str = "123";
        String message_str = "ABCD";
        byte message_type = 2;

        byte[] hdr = new byte[] {-33,0,15,2,1,18,0,20,1,0,0};
        byte[] sender = sender_str.getBytes(StandardCharsets.UTF_8);
        byte[] message = message_str.getBytes(StandardCharsets.UTF_8);
        byte[] in = new byte[hdr.length + sender.length + 1 + message.length];

        if (in.length > 200) {
            System.out.println("Payload too long!");
            return;
        }

        int ptr = 0;
        System.arraycopy(hdr, 0, in, ptr, hdr.length);
        ptr += hdr.length;
        System.arraycopy(sender, 0, in, ptr, sender.length);
        ptr += sender.length;
        in[ptr] = ':';
        ptr += 1;
        System.arraycopy(message, 0, in, ptr, message.length);
        int payloadlen = sender.length + 1 + message.length;
        in[1] = (byte) ((payloadlen + 8) >> 8);
        in[2] = (byte) ((payloadlen + 8) & 255);
        in[6] = (byte) ((payloadlen + 3) >> 8);
        in[7] = (byte) ((payloadlen + 3) & 255);
        in[8] = message_type;

        byte[] out = HelloWorld.addSumCheck(in);
        System.out.println(bytesToHex(out));
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
}

    public static byte[] addSumCheck(byte[] bytes) {
        int i = 0;
        for (byte b : bytes) {
            i += b;
        }
        byte[] bArr = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, bArr, 0, 3);
        bArr[3] = (byte) (i & 255);
        System.arraycopy(bytes, 3, bArr, 4, bytes.length - 3);
        return bArr;
    }
}


