package com.iskrembilen.quasseldroid;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import com.iskrembilen.quasseldroid.Quasseldroid;
import com.iskrembilen.quasseldroid.R;
import com.iskrembilen.quasseldroid.io.QuasselDbHelper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class QuasseldroidDbHelperTest {

	private QuasselDbHelper dbHelper;

	@Before
	public void openDb() {
		dbHelper = new QuasselDbHelper(Robolectric.getShadowApplication().getApplicationContext());
		dbHelper.open();
	}

	@After
	public void closeDb() {
		dbHelper.close();
		dbHelper = null;
	}

	@Test
	public void userRemovedOnCoreDeleteTest() throws Exception {
		long coreId = addCore();
		dbHelper.addUser("user", "1234", coreId);
		Assert.assertNotNull(dbHelper.getUser(coreId));

		dbHelper.deleteCore(coreId);
		Assert.assertNull(dbHelper.getUser(coreId));
	}

	@Test
	public void deleteUserTest() throws Exception {
		long coreId = addCore();
		dbHelper.addUser("user", "1234", coreId);
		Assert.assertNotNull(dbHelper.getUser(coreId));

		dbHelper.deleteUser(coreId);
		Assert.assertNull(dbHelper.getUser(coreId));
	}

	@Test
	public void usersAreUniqueAndOverridesOldEntriesTest() throws Exception {
		long coreId = addCore();
		SQLiteDatabase db = dbHelper.getDatabase();
		dbHelper.addUser("user", "1234", coreId);
		Assert.assertNotNull(dbHelper.getUser(coreId));
		
		dbHelper.addUser("user2", "2345", coreId);
		Cursor c = db.query(QuasselDbHelper.USER_TABLE, new String[] {QuasselDbHelper.KEY_USERNAME,QuasselDbHelper.KEY_PASSWORD}, null, null, null, null, null);
		Assert.assertEquals(1, c.getCount());
		c.moveToFirst();
		Assert.assertEquals("user2", c.getString(c.getColumnIndex(QuasselDbHelper.KEY_USERNAME)));
		c.close();
	}
	
	@Test
	public void certificateRemovedOnCoreDeleteTest() throws Exception {
		long coreId = addCore();
		String hash = "hash";
		dbHelper.storeCertificate(hash, coreId);
		Assert.assertEquals("hash", dbHelper.getCertificate(coreId));

		dbHelper.deleteCore(coreId);
		Assert.assertNull(dbHelper.getCertificate(coreId));
	}
	
	@Test
	public void certificatesAreUniqueAndOverridesOldEntriesTest() throws Exception {
		long coreId = addCore();
		SQLiteDatabase db = dbHelper.getDatabase();
		dbHelper.storeCertificate("hash", coreId);
		Assert.assertEquals("hash", dbHelper.getCertificate(coreId));
		
		dbHelper.storeCertificate("hash2", coreId);
		Cursor c = db.query(QuasselDbHelper.CERTIFICATE_TABLE, new String[] {QuasselDbHelper.KEY_CERTIFICATE}, null, null, null, null, null);
		Assert.assertEquals(1, c.getCount());
		c.close();
		System.out.println(dbHelper.getCertificate(coreId));
		Assert.assertEquals("hash2", dbHelper.getCertificate(coreId));
	}

	private long addCore() {
		dbHelper.addCore("test", "test.com", 80, true);
		Cursor c = dbHelper.getAllCores();
		c.moveToFirst();
		long coreId = c.getLong(c.getColumnIndex(QuasselDbHelper.KEY_ID));
		c.close();
		return coreId;
	}

}