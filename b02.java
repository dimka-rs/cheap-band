import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

class HelloWorld {
    public static String MAC = "02:FC:25:D7:6F:E3";
    public static String HANDLE = "0x0034";
    // currently only 8 chars of payload fit, maybe MTU issue, MTU is 20
    // need to call gatttool several times
    public static String sender_str  = "Sender";
    public static String message_str = "123456789A123456789B123456789C123456789D123456789E123456789F";
    public static byte message_type = 2;
    public static byte mtu = 20;

    public static void main(String[] args) {
        //byte[] in  = new byte[] {-33,0,5,2,1,17,0,0};// call end
        //byte[] in  = new byte[] {-33,0,15,2,1,18,0,20,1,0,0,'1','2','3',':','A','B','C'};// sms

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
        //System.out.println(bytesToHex(out));

        boolean isLinux = System.getProperty("os.name").toLowerCase().startsWith("linux");
        if (isLinux) {

            while (out.length > mtu) {
                byte[] toSend = new byte[mtu];
                System.arraycopy(out, 0, toSend, 0, mtu);
                int length = out.length - mtu;
                byte[] remaining = new byte[length];
                System.arraycopy(out, mtu, remaining, 0, length);
                if (!send(toSend)) {
                    return;
                };
                try {
                    Thread.sleep(20L);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                out = remaining;
            }
            send(out);
        }
    }

    static boolean send(byte[] data) {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("gatttool", "--device="+MAC, "--char-write-req", "--handle="+HANDLE, "--value="+bytesToHex(data));
        System.out.println(String.join(" ",pb.command().toArray(new String[0])));

        try {
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            System.out.println("Exit: " + String.valueOf(exitVal));
            if (exitVal != 0) {
                return false;
            }
            System.out.println(output);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return true;
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


