package com.nfc.reader.handle;
import com.nfc.reader.SpecConf;
import com.nfc.reader.Util;
import com.nfc.reader.bean.CardApplications;
import com.nfc.reader.tech.Iso7816;

import android.annotation.SuppressLint;

final class CityTransitHandle extends ProtocolAssembler {
	private Object applicationId = SpecConf.APP.UNKNOWN;

	@Override
	protected Object getApplicationId() {
		return applicationId;
	}

	@Override
	protected byte[] getMainApplicationId() {
		return new byte[] { (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03,
				(byte) 0x86, (byte) 0x98, (byte) 0x07, (byte) 0x01, };
	}

	@SuppressLint("DefaultLocale")
	@Override
	protected void parseInfo21(CardApplications app, Iso7816.Response data, int dec, boolean bigEndian) {

		if (!data.isOkey() || data.size() < 30) {
			return;
		}

		final byte[] d = data.getBytes();

		if (d[2] == 0x20 && d[3] == 0x00) {
			applicationId = SpecConf.APP.SHANGHAIGJ;
			bigEndian = true; 
		} else {
			applicationId = SpecConf.getCityUnionCardNameByZipcode(Util.toHexString(d[2], d[3]));
			bigEndian = false;
		}

		if (dec < 1 || dec > 10) {
			app.setProperty(SpecConf.PROP.SERIAL, Util.toHexString(d, 10, 10));
		} else {
			final int sn = Util.toInt(d, 20 - dec, dec);
			final String ss = bigEndian ? Util.toStringR(sn) : String
					.format("%d", 0xFFFFFFFFL & sn);
			app.setProperty(SpecConf.PROP.SERIAL, ss);
		}

		if (d[9] != 0)
			app.setProperty(SpecConf.PROP.VERSION, String.valueOf(d[9]));

		app.setProperty(SpecConf.PROP.DATE, String.format("%02X%02X.%02X.%02X - %02X%02X.%02X.%02X",
				d[20], d[21], d[22], d[23], d[24], d[25], d[26], d[27]));
	}
}
