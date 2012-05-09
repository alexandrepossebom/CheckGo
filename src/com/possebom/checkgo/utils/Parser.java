package com.possebom.checkgo.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.util.ByteArrayBuffer;

import com.possebom.checkgo.model.Card;
import com.possebom.checkgo.model.Item;

public class Parser {
	public static void updateCard(final Card card) {
		String result = "";
		try {
			URL url = new URL("http://www.cbss.com.br/inst/convivencia/SaldoExtrato.jsp?numeroCartao=" + card.getNumber() + "&periodoSelecionado=4");
			URLConnection connection = url.openConnection();
			InputStream inputStream = connection.getInputStream();
			BufferedInputStream bufferedInput = new BufferedInputStream(inputStream);
			ByteArrayBuffer byteArray = new ByteArrayBuffer(50);
			int current = 0;
			while((current = bufferedInput.read()) != -1){
				byteArray.append((byte)current);
			}
			result = new String(byteArray.toByteArray(), "ISO-8859-1");
			inputStream.close();
		} catch (Exception e) {
			return;
		}

		if(result == null || result.length() == 0) return;

		List<Item> list = new ArrayList<Item>();

		String fmt = "dd/MM";
		SimpleDateFormat sdf = new SimpleDateFormat(fmt);  
		
		Pattern pSaldo = Pattern.compile("<tr class=\"rowTable\">" +
				"<td class=\"corUm fontWeightDois\" align=\"right\">" +
				"Saldo dispon\\&iacute\\;vel:</td>" +
				"<td class=\"corUm fontWeightDois\" style=\"width:80px\" align=\"right\">" +
				"R\\$ (.*?)" +
				"</td></tr>",Pattern.UNIX_LINES|Pattern.MULTILINE);
		Matcher mSaldo = pSaldo.matcher(result);
		if (mSaldo.find()) {
			try {  
				card.setTotal(Float.valueOf(mSaldo.group(1).replaceAll("\\.", "").replace(",",".")));
	        } catch (Exception ex) {  
	        	card.setTotal(0);
	        }
		}

		Pattern pLast = Pattern.compile("<td width=\"300px\">" +
				"Data da \\&uacute\\;ltima disponibiliza\\&ccedil\\;\\&atilde\\;o do benef\\&iacute\\;cio:" +
				"</td>\r\n.*?<td class=\"corUm\" align=\"left\">(.*?)</td>\r\n.*?<td class=\"corUm\" align=\"right\">" +
				"Valor:R\\$(.*?)</td>",Pattern.UNIX_LINES|Pattern.MULTILINE);
		
		Matcher mLast = pLast.matcher(result);
		if (mLast.find()) {
			try {  
				card.setLastCharge(sdf.parse(mLast.group(1)));
			} catch (ParseException ex) {  
				card.setLastCharge(null);
			}
			try{
				card.setLastChargeValor(Float.valueOf(mLast.group(2).replaceAll("\\.", "").replace(",",".")));
			}catch (Exception e) {
				card.setLastChargeValor(0);
			}
		}

		Pattern pNext = Pattern.compile("<td width=\"300px\">" +
				"Data da pr\\&oacute\\;xima disponibiliza\\&ccedil\\;\\&atilde\\;o do benef\\&iacute\\;cio:" +
				"</td>\r\n.*?<td class=\"corUm\" align=\"left\">(.*?)</td>\r\n.*?" +
				"<td class=\"corUm\" align=\"right\">Valor:R\\$(.*?)</td>",Pattern.UNIX_LINES|Pattern.MULTILINE);
		Matcher mNext = pNext.matcher(result);
		if (mNext.find()) {
			try {  
				card.setNextCharge((sdf.parse(mNext.group(1))));
			} catch (ParseException ex) {  
				card.setNextCharge(null);
			}  
			try{
			card.setNextChargeValor(Float.valueOf(mNext.group(2).replaceAll("\\.", "").replace(",",".")));
			}catch (Exception e) {
				card.setNextChargeValor(0);
			}
		}

		Pattern pItem = Pattern.compile(".*<td style=\"width:50px\">(.*?)</td>\r\n.*?<td style=\"width:400px\">(.*?)" +
				"</td>\r\n.*?<td class=\"corUm\" style=\"width:50px\" align=\"right\">" +
				"R\\$\\&nbsp\\;(.*?)</td>", Pattern.UNIX_LINES|Pattern.MULTILINE);
		Matcher mItem = pItem.matcher(result);
		while (mItem.find()) {
			Item i = new Item();
			i.setItemCard(card.getNumber());
			try {
				i.setDia(sdf.parse(mItem.group(1)));
			} catch (ParseException ex) {  
				i.setDia(null);
			}  
			i.setPlace(mItem.group(2));
			if(i.getPlace().equals("Disponibilização de Beneficio"))
				i.setCharge(true);
			try{
				i.setValor(Float.valueOf(mItem.group(3).replaceAll("\\.", "").replace(",",".")));
			}catch (Exception e) {
				i.setValor(0);
			}
			list.add(i);
		}
		card.setItens(list);
	}
}
