/* NFCard is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

NFCard is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Wget.  If not, see <http://www.gnu.org/licenses/>.

Additional permission under GNU GPL version 3 section 7 */

package com.nfc.reader.ui;

import java.util.Collection;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;

import com.nfc.reader.R;
import com.nfc.reader.ThisApplication;
import com.nfc.reader.SPEC.EVENT;
import com.nfc.reader.bean.Application;
import com.nfc.reader.bean.Card;
import com.nfc.reader.rd.ReaderListener;

public final class NfcPage implements ReaderListener {
	private static final String TAG = "READCARD_ACTION";//读卡动作
	private static final String RET = "READCARD_RESULT";//读卡结果  html对象
	private static final String DATA = "READCARD_DATA";//原始卡数据对象
	private static final String STA = "READCARD_STATUS";//读卡成功状态

	private final Activity activity;

	public NfcPage(Activity activity) {
		this.activity = activity;
	}

	public static boolean isSendByMe(Intent intent) {
		return intent != null && TAG.equals(intent.getAction());
	}

	public static boolean isNormalInfo(Intent intent) {
		return intent != null && intent.hasExtra(STA);
	}

	public static CharSequence getContent(Activity activity, Intent intent) {

		String info = intent.getStringExtra(RET);
		if (info == null || info.length() == 0)
			return null;

		return new SpanFormatter(AboutPage.getActionHandler(activity))
				.toSpanned(info);
	}
	//返回Application 集
	public static Collection<Application> getDATAContent(Activity activity, Intent intent) {

		Card card = (Card)intent.getSerializableExtra(DATA);
		if (card == null)
			return null;

		return card.getApplications();
	}

	@Override
	public void onReadEvent(EVENT event, Object... objs) {
		if (event == EVENT.IDLE) {
			showProgressBar();
		}else if(event==EVENT.READING){
			showProgressBar();
		} 
		else if (event == EVENT.FINISHED) {
			hideProgressBar();

			final Card card;
			if (objs != null && objs.length > 0)
				card = (Card) objs[0];
			else
				card = null;
			//读取成功后设置activity的Intent
			activity.setIntent(buildResult(card));
		}
	}
	/**
	 * 构建最终结果集
	 * @param card
	 * @return
	 */
	private Intent buildResult(Card card) {
		//Read action Intent
		final Intent ret = new Intent(TAG);

		if (card != null && !card.hasReadingException()) {
			if (card.isUnknownCard()) {
				ret.putExtra(RET, ThisApplication
						.getStringResource(R.string.info_nfc_unknown));
			} else {
				//成功读取卡信息结果
				ret.putExtra(RET, card.toHtml());
				ret.putExtra(DATA, card);
				ret.putExtra(STA, 1);
			}
		} else {
			ret.putExtra(RET,
					ThisApplication.getStringResource(R.string.info_nfc_error));
		}

		return ret;
	}

	private void showProgressBar() {
		Dialog d = progressBar;
		if (d == null) {
			d = new Dialog(activity, R.style.progressBar);
			d.setCancelable(false);
			d.setContentView(R.layout.progress);
			progressBar = d;
		}

		if (!d.isShowing())
			d.show();
	}

	private void hideProgressBar() {
		final Dialog d = progressBar;
		if (d != null && d.isShowing())
			d.cancel();
	}

	private Dialog progressBar;
}
