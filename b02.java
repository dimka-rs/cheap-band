import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Calendar;

class HelloWorld {
    public static String MAC = "02:FC:25:D7:6F:E3";
    /* notify "6E400003-B5A3-F393-E0A9-E50E24DCCA9F" */
    public static String HANDLE_NF = "0x0030";
    /* write "6E400002-B5A3-F393-E0A9-E50E24DCCA9F" */
    public static String HANDLE_WR = "0x0034";
    /* band can only handle 20 bytes at a time */
    public static byte mtu = 20;

    //public static String message = "123456789A123456789B123456789C123456789D123456789E123456789F123456789G";
    public static String message = "123456789A123456789B";

    public static void main(String[] args) {

        //notifyMessage(1, message);
        //notifyCall(2, message);
        //notifyCall(1, "");

        //settingSysTime();
        findBand();
        //System.out.printf("Battery: %d%%%n", getBattery());

    }

    static void send(byte[] data) {
        //System.out.println(bytesToHex(out));
        if (data[0] == -33)
        {
            data = HelloWorld.addSumCheck(data);
        }

        boolean isLinux = System.getProperty("os.name").toLowerCase().startsWith("linux");
        if (isLinux) {

            while (data.length > mtu) {
                byte[] toSend = new byte[mtu];
                System.arraycopy(data, 0, toSend, 0, mtu);
                int length = data.length - mtu;
                byte[] remaining = new byte[length];
                System.arraycopy(data, mtu, remaining, 0, length);
                if (!sendChunk(toSend)) {
                    return;
                };
                try {
                    Thread.sleep(20L);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                data = remaining;
            }
            sendChunk(data);
        }
    }

    static boolean sendChunk(byte[] data) {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("gatttool", "--device="+MAC, "--char-write-req", "--handle="+HANDLE_WR, "--value="+bytesToHex(data));
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

    static String readChar(String uuid) {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("gatttool", "--device="+MAC, "--char-read", "--uuid="+uuid);
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
            System.out.println(output);
            return output.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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

    public static byte[] getSendByte(byte commandId, byte key, byte[] value) {
        byte[] bArr = new byte[value.length + 8];
        bArr[0] = -33; // header
        bArr[1] = (byte) ((value.length + 5) >> 8);
        bArr[2] = (byte) ((value.length + 5) & 255);
        bArr[3] = commandId;
        bArr[4] = 1; //protocol version
        bArr[5] = key;
        bArr[6] = (byte) (value.length >> 8);
        bArr[7] = (byte) (value.length & 255);
        System.arraycopy(value, 0, bArr, 8, value.length);
        return bArr;
    }

    public static byte[] getSendByte(byte commandId, byte key) {
        return new byte[]{-33, 0, 5, commandId, 1, key, 0, 0};
    }

    public static void settingSysTime() {
        Calendar calendar = Calendar.getInstance();
        int i = calendar.get(1); // Year
        int i2 = calendar.get(2) + 1; // month
        int i3 = calendar.get(5); // date
        int i4 = calendar.get(11); // hour
        int i5 = calendar.get(12); // minute
        byte[] data = getSendByte((byte) 2, (byte) 1, new byte[]{(byte) (((i - 2000) << 2) + ((i2 & 255) >> 2)), (byte) (((i2 & 3) << 6) + (i3 << 1) + (i4 >> 4)), (byte) (((i4 & 15) << 4) + (i5 >> 2)), (byte) (((i5 & 3) << 6) + calendar.get(13))});
        send(data);
    }

    public static void notifyMessage(int type, String message_str)
    {
        /*  1 - sms, 2 - qq, 3 - wechat, 4 - facebook, 5 - twitter
            6 - skype, 7 - line, 8 - whatsapp, 9 - talk, 10 - instagram
            other values will work, but no icon will appear */
        byte[] bytes = message_str.getBytes(StandardCharsets.UTF_8);

        if (bytes.length > 196) {
            System.out.println("Payload too long!");
            return;
        }

        byte[] data = new byte[bytes.length + 3];
        data[0] = (byte) type;
        System.arraycopy(bytes, 0, data, 3, bytes.length);
        send(getSendByte((byte) 2, (byte) 18, data));
    }

    public static void notifyCall(int type, String message_str)
    {
        if (message_str.length() > 0)
        {
            /* Ringing */
            byte[] bytes = message_str.getBytes(StandardCharsets.UTF_8);
            byte[] data = new byte[bytes.length + 2];
            /* 0 - hangup, 1 - ringing, 2 - ongoing? */
            data[0] = (byte) type;
            data[1] = 0;
            for (int i = 0; i < bytes.length; i++)
            {
                data[i + 2] = bytes[i];
            }
            send(getSendByte((byte) 2, (byte) 17, data));
        } else {
            /* Hangup */
            send(getSendByte((byte) 2, (byte) 17));
        }
    }

    public static int getBattery()
    {
        String out = readChar("0x2a19");
        int idx = out.indexOf("value: ");

        if (idx != -1) {
            try {
                int val = Integer.parseInt(out.substring(idx + 7).trim(), 16);

                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer input");
            }

        }
        return -1;
    }

    public static void findBand()
    {
        send(getSendByte((byte) 2, (byte) 11));
    }

}


