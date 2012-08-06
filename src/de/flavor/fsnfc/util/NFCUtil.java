package de.flavor.fsnfc.util;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Random;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;

public class NFCUtil {

	private static NdefMessage createNdefMessage(NdefRecord... records) {
		NdefMessage ndefMessage = new NdefMessage(records);
		return ndefMessage;
	}

	// support for NdefMessage with single Mime Record
	public static NdefMessage getNdefMimeMessage(String mimeType, String content) {
		NdefRecord mimeRecord = getMimeRecord(mimeType, content);
		return createNdefMessage(mimeRecord);
	}
		
	// support for NdefMessage with well known TEXT
	public static NdefMessage getNdefWellKnownText(String text)
	{
		NdefRecord textRecord = getTextRecord(text);
		return createNdefMessage(textRecord);
	}
	
	// support for NdefMessage with well known URI
	public static NdefMessage getNdefWellKnownURI(String uri)
	{
		NdefRecord uriRecord = NdefRecord.createUri(uri);
		return createNdefMessage(uriRecord);
	}	
	
	private static NdefRecord getMimeRecord(String mimeType, String content) {
		NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				mimeType.getBytes(), getRandomIdBytes(), content.getBytes());
		return record;
	}


	private static byte[] getRandomIdBytes() {
		Random rand = new Random(System.currentTimeMillis());
		byte buf[] = new byte[4];
		rand.nextBytes(buf);
		return buf;
	}


	private static NdefRecord getTextRecord(String message) {
		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_TEXT, getRandomIdBytes(),
				getTextPayLoad(message));
		return record;
	}

	//old fashioned...
	private static NdefRecord getURIRecord(String uri) {
		NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_URI, getRandomIdBytes(), getURIPayLoad(uri));
		return record;
	}

	public static byte[] getURIPayLoad(String uriNoHttp) {
		byte rawBytes[] = uriNoHttp.getBytes();
		byte httpPrefixByte[] = new byte[] { (byte) 0x03 };
		byte uriBytes[] = new byte[rawBytes.length + 1];

		copySmallArraysToBigArray(new byte[][] { httpPrefixByte, rawBytes },
				uriBytes);
		
		return uriBytes;
	}
	
	/**
	 * payload[0] contains the "Status Byte Encodings" field, per the NFC Forum
	 * "Text Record Type Definition" section 3.2.1.
	 * 
	 * bit7 is the Text Encoding Field.
	 * 
	 * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1): The text
	 * is encoded in UTF16
	 * 
	 * Bit_6 is reserved for future use and must be set to zero.
	 * 
	 * Bits 5 to 0 are the length of the IANA language code.
	 */
	public static byte[] getTextPayLoad(String message) {

		Locale locale = Locale.US;
		final byte[] langBytes = locale.getLanguage().getBytes(
				Charset.forName("UTF-8"));

		final byte[] textBytes = message.getBytes(Charset.forName("UTF-8"));
		final int utfBit = 0;
		final char status = (char) (utfBit + langBytes.length);

		int totalLength = 1 + langBytes.length + textBytes.length;
		byte data[] = new byte[totalLength];

		copySmallArraysToBigArray(new byte[][] { new byte[] { (byte) status },
				langBytes, textBytes }, data);

		return data;
	}

	public static void copySmallArraysToBigArray(final byte[][] smallArrays,
			final byte[] bigArray) {
		int currentOffset = 0;
		for (final byte[] currentArray : smallArrays) {
			System.arraycopy(currentArray, 0, bigArray, currentOffset,
					currentArray.length);
			currentOffset += currentArray.length;
		}
	}

	public static boolean supportsNdef(Tag tag) {

		String techs[] = tag.getTechList();
		for (String tech : techs) {
			if (tech.equals("android.nfc.tech.Ndef"))
				return true;
		}

		return false;
	}

	public static boolean supportsNdefFormatable(Tag tag) {

		String techs[] = tag.getTechList();
		for (String tech : techs) {
			if (tech.equals("android.nfc.tech.NdefFormatable"))
				return true;
		}

		return false;
	}

}
