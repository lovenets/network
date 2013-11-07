package org.wjd.net;

import org.wjd.business.audio.AudioRecPlay;
import org.wjd.business.base.BusMessage;
import org.wjd.business.push.BusiPushHandler;
import org.wjd.net.tcp_udp.BaseMessage;
import org.wjd.net.tcp_udp.ChannelProxy;
import org.wjd.net.tcp_udp.ChannelProxy.CHANNEL_TYPE;
import org.wjd.net.tcp_udp.NetErrorHandler;
import org.wjd.net.tcp_udp.NormalHandler;
import org.wjd.net.tcp_udp.UnsyncRequest;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements NetErrorHandler,
		NormalHandler
{

	private EditText editContent;

	private TextView tvReceived;

	// private TextView tvNetStatus;

	private ChannelProxy cProxy;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editContent = (EditText) findViewById(R.id.edit_content);
		findViewById(R.id.btn_send).setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						onSend();
					}
				});
		tvReceived = (TextView) findViewById(R.id.tv_received);
		// tvNetStatus = (TextView) findViewById(R.id.tv_net_status);
		// 修改此处参数即可分别测试udp和tcp
		cProxy = new ChannelProxy(CHANNEL_TYPE.TYPE_UDP);
		// cProxy.setChannelConnectionStatusChangedListener(new
		// TcpConnectHandler()
		// {
		// @Override
		// public void handleTcpConnectResult(boolean connected)
		// {
		// tvNetStatus.setText(connected ? "On" : "Off");
		// }
		// });
		cProxy.setPushHandler(new BusiPushHandler());
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				cProxy.init("127.0.0.1", 10011);
			}
		}).start();
		new AudioRecPlay().startRecord();
	}

	private void onSend()
	{
		String content = editContent.getText().toString();
		if (TextUtils.isEmpty(content))
		{
			return;
		}
		UnsyncRequest request = new UnsyncRequest(this, this, "127.0.0.1",
				10011);
		byte[] data = content.getBytes();
		BaseMessage message = new BusMessage((byte) 1, (byte) 1, 12345,
				(byte) 2, data);
		request.setMessage(message);
		cProxy.sendMessage(request);
	}

	@Override
	public void handleResponse(BaseMessage message)
	{
		tvReceived.setText(message.toString());
	}

	@Override
	public void handleNetError(BaseMessage message)
	{
		// TODO byte[] data = new byte[message.getData().length - 10];
		// ByteBuffer buffer = ByteBuffer.wrap(message.getData());
		// buffer.getShort();
		// buffer.getLong();
		// buffer.get(data, 0, data.length);
		// tvReceived.setText("Send Error: " + new String(data));
	}

	@Override
	protected void onDestroy()
	{
		if (null != cProxy)
		{
			cProxy.unInit();
		}
		super.onDestroy();
	}
}
