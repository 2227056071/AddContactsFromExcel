package com.example.user.myapplication;

import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ImportContactsActivity extends AppCompatActivity {

    private static final String TAG = "ImportContactsActivity";
    private FileListCursorAdapter mAdapter;
    private List<ContactInfo> mContactInfoList = new ArrayList<ContactInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Cursor cursor = getXlsFiles();
        ListView listView = findViewById(R.id.file_path_list);
        mAdapter = new FileListCursorAdapter(this, cursor, 0);
        listView.setAdapter(mAdapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long
                    id) {
                showPopWindows(view);
//                FileInfo fileInfo = mAdapter.getFileItem(position);
//                String filePath = fileInfo.filePath;
//                File file = new File(filePath);
//                getXlsFileDataByJxl(file);
//                addContacts();
                return true;
            }
        });
    }

    private void getXlsFileDataByJxl(File file) {
        Workbook book = null;//用读取到的表格文件来实例化工作簿对象（符合常理，我们所希望操作的就是Excel工作簿文件）
        try {
            book = Workbook.getWorkbook(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        Sheet[] sheets = book.getSheets(); //得到所有的工作表
        for (int m = 0; m < sheets.length; m++) {
            Sheet sheet = book.getSheet(m);
            int Rows = sheet.getRows();//得到当前工作表的行数
            int Cols = sheet.getColumns(); //得到当前工作表的列数
            for (int i = 0; i < Rows; i++) {  // 注意：这里是按行读取的！！！
                String name = sheet.getCell(0, i).getContents(); //默认第一列是联系人姓名
                String phone = sheet.getCell(1, i).getContents();//默认第一列是联系人电话号码
                ContactInfo contactInfo = new ContactInfo();
                contactInfo.setName(name);
                contactInfo.setPhoneNumber(phone);
                mContactInfoList.add(contactInfo);
            }
        }
    }

    private void addContacts() {
        for (int i = 0; i < mContactInfoList.size(); i++) {
            ContactInfo contactInfo = mContactInfoList.get(i);
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            //------------------------------------------------------ Names
            if (contactInfo.getName() != null) {
                ops.add(ContentProviderOperation.newInsert(
                        ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                contactInfo.getName()).build());
            }

            //------------------------------------------------------ Mobile Number
            if (contactInfo.getPhoneNumber() != null) {
                ops.add(ContentProviderOperation.
                        newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactInfo
                                .getPhoneNumber())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
            }
            // Asking the Contact provider to create a new contact
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Cursor getXlsFiles() {
        String volumeName = "external";
        Uri uri = MediaStore.Files.getContentUri(volumeName);
        String selection = "(mime_type=='application/vnd.openxmlformats-officedocument" +
                ".spreadsheetml.sheet') OR (mime_type=='application/vnd.ms-excel')";
        String sortOrder = MediaStore.Files.FileColumns.TITLE + " asc";
        String[] columns = new String[]{
                MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATA, MediaStore
                .Files.FileColumns.SIZE, MediaStore.Files.FileColumns.DATE_MODIFIED
        };
        Cursor cursor = getContentResolver().query(uri, columns, selection, null, sortOrder);
        return cursor;
    }


    private PopupWindowList mPopupWindowList;

    private void showPopWindows(View view) {
        List<String> dataList = new ArrayList<>();
        dataList.add(getResources().getString(R.string.item_import));
        dataList.add(getResources().getString(R.string.item_share));
        if (mPopupWindowList == null) {
            mPopupWindowList = new PopupWindowList(view.getContext());
        }
        mPopupWindowList.setAnchorView(view);
        mPopupWindowList.setItemData(dataList);
        mPopupWindowList.setModal(true);
        mPopupWindowList.show();
        mPopupWindowList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "click position=" + position);
                mPopupWindowList.hide();
            }
        });
    }
}
