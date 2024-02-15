import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    private static final String DNS_SERVER_ADDRESS = "8.8.8.8";
    private static final int DNS_SERVER_PORT = 53;
    private static final int ID_TRANSACTION = 1111;
    //0-query 1-response
    private static final String QR = "0";
    private static final String QUERY = "0000";
    private static final String AA = "0";
    private static final String TC = "0";
    private static final String RD = "1";
    private static final String RA = "0";
    private static final String Z = "000";
    private static final String RDCODE = "0000";
    private static final short QDCOUNT = 1;
    private static final short ANCOUNT = 0;
    private static final short NSCOUNT = 0;
    private static final short ARCOUNT = 0;
    private static final short QTYPE = 1;
    private static final short QCLASS = 1;



    public static void main(String[] args) throws IOException {
        ArrayList<String> domains = new ArrayList<>(Arrays.asList(args));

        InetAddress ipAddress = InetAddress.getByName(DNS_SERVER_ADDRESS);

        for(String domain: domains) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            customDNSRequest(out, domain);

            byte[] dnsFrame = out.toByteArray();
            try(DatagramSocket socket = new DatagramSocket()) {
                DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, ipAddress, DNS_SERVER_PORT);
                socket.send(dnsReqPacket);

                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                DataInputStream in = new DataInputStream(new ByteArrayInputStream(buf));
                DataResponse response = handlerResponse(in);

                StringBuilder findingIPAddress = new StringBuilder("IP address ");
                findingIPAddress.append(domain).append(": ");
                for(int index = 0; index < response.responseDataLength; index++) {
                    findingIPAddress.append(response.responseData[index]);
                    if(index < (response.responseDataLength - 1)) {
                        findingIPAddress.append(".");
                    }
                }
                System.out.println(findingIPAddress);
            } catch (SocketException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void customDNSRequest(ByteArrayOutputStream out, String domain) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeShort(ID_TRANSACTION);

        String flags = QR + QUERY + AA + TC + RD + RA + Z + RDCODE;
        short requestFlags = Short.parseShort(flags, 2);
        ByteBuffer flagsByteBuffer = ByteBuffer.allocate(2).putShort(requestFlags);
        byte[] flagsByteArray= flagsByteBuffer.array();

        dos.write(flagsByteArray);

        dos.writeShort(QDCOUNT);
        dos.writeShort(ANCOUNT);
        dos.writeShort(NSCOUNT);
        dos.writeShort(ARCOUNT);

        String[] domainParts = domain.split("\\.");

        for (String domainPart : domainParts) {
            byte[] domainBytes = domainPart.getBytes(StandardCharsets.UTF_8);
            dos.writeByte(domainBytes.length);
            dos.write(domainBytes);
        }
        dos.writeByte(0x00);

        dos.writeShort(QTYPE);
        dos.writeShort(QCLASS);
    }

    private static DataResponse handlerResponse(DataInputStream in) throws IOException {
        int transaction = in.readShort();
        int flags = in.readShort();
        int questionsSection = in.readShort();
        int answersSection = in.readShort();
        int authorityRecordSection = in.readShort();
        int additionalRecordSection = in.readShort();

        ArrayList<String> questionName = new ArrayList<>();
        int recLen = 0;
        while ((recLen = in.readByte()) > 0) {
            byte[] record = new byte[recLen];

            for (int i = 0; i < recLen; i++) {
                record[i] = in.readByte();
            }

            questionName.add(new String(record, StandardCharsets.UTF_8));
        }

        int questionType = in.readShort();
        int questionClass = in.readShort();

        int resourceRecord = in.readShort();
        int recordType = in.readShort();
        int recordClass = in.readShort();
        int TTL = in.readInt();
        short responseDataLength = in.readShort();

        ArrayList<Integer> responseData = new ArrayList<>();
        for (int i = 0; i < responseDataLength; i++) {
            responseData.add(in.readByte() & 0xFF);
        }

        return new DataResponse(
                transaction,
                flags,
                questionsSection,
                answersSection,
                authorityRecordSection,
                additionalRecordSection,
                questionName.toArray(String[]::new),
                questionType,
                questionClass,
                resourceRecord,
                recordType,
                recordClass,
                TTL,
                responseDataLength,
                responseData.stream().mapToInt(i -> i).toArray()
        );
    }
}