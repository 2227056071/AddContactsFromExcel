package com.example.user.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class NewImportContactsFragment extends Fragment {
    private static final int ITEM_IMPORT_CONTACTS = 0;
    private List<ContactInfo> mContactInfoList = new ArrayList<ContactInfo>();
    static final String TAG1 = "ContactsFragment";
    List<FileInfo> mFileInfoList = new ArrayList<FileInfo>();

    private View mFragmentContainer;
    private LinearLayoutManager mLayoutManager;
    private View mSelectedItemView;
    private CustomFileAdapter mAdapter;
    private String mSelectedItemFilePath;
    private ProgressBar mProgressBar;
    private PopupWindowList mPopupWindowList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mFileInfoList.clear(); // 刷新頁面时清空数据
        mFragmentContainer = inflater.inflate(R.layout.activity_new_import_contacts, container, false);
        initUi();
        queryExcelFiles();
        return mFragmentContainer;
    }

    private void initUi() {
        mFragmentContainer.setBackgroundColor(Color.WHITE);
        final RecyclerView recyclerView = mFragmentContainer.findViewById(R.id.recyclerview_import_fragment);
        mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new CustomFileAdapter(mFileInfoList, new OnRecycleViewItemListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void OnRecycleViewItemLongClick(View view, FileInfo fileInfo) {
                mSelectedItemView = view;
                view.setBackgroundColor(android.graphics.Color.rgb(178, 178, 178));
                mSelectedItemFilePath = fileInfo.filePath;
                showPopWindows(view);
            }

            @Override
            public void OnRecycleViewItemClick(FileInfo fileInfo) {
                Intent intent = getExcelFileIntent(fileInfo.filePath);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mProgressBar = mFragmentContainer.findViewById(R.id.progress_bar_import_contants);
    }

    private void queryExcelFiles() {
        new QueryFileAsyncTask().execute();
    }

    private class QueryFileAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            mFileInfoList = getFileInfoList(); // 遍历本地所有文件，属于耗时操作
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private class ImportContactsAsyncTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            addContacts(); // 写通讯录，属于耗时操作
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "导入完成", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private List<FileInfo> getFileInfoList() {
        String volumeName = "external";
        Uri uri = MediaStore.Files.getContentUri(volumeName);
        String sortOrder = MediaStore.Files.FileColumns.TITLE + " desc";
        String[] columns = new String[]{
                MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATA, MediaStore
                .Files.FileColumns.SIZE, MediaStore.Files.FileColumns.DATE_MODIFIED
        };
        // selection 设置为 null
        Cursor cursor = getContext().getContentResolver().query(uri, columns, null, null,
                sortOrder);
        while (cursor.moveToNext()) {
            FileInfo fileInfo = new FileInfo();
            String filePath = cursor.getString(FileCategoryHelper.COLUMN_PATH);
            String fileName = Util.getNameFromFilepath(filePath);
            Long fileSize = cursor.getLong(FileCategoryHelper.COLUMN_SIZE);
            Long modifyTime = cursor.getLong(FileCategoryHelper.COLUMN_DATE);
            // 因部分 excel 文件的 mime_type 为 null,此处使用后缀判断
            if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                fileInfo.fileName = fileName;
                fileInfo.ModifiedDate = modifyTime;
                fileInfo.fileSize = fileSize;
                fileInfo.filePath = filePath;
                mFileInfoList.add(fileInfo);
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
        return mFileInfoList;
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
            int nameColumnNumber = 0;
            int mobileColumnNumber = -1;
            int phoneColumnNumber = -1;
            for (int i = 0; i < Cols; i++) {
                String name = sheet.getCell(i, 0).getContents();
                if (!Util.isEmpty(name))
                {
                    if (name.contains(getResources().getString(R.string.phone)))
                    {
                        phoneColumnNumber = i;
                    }
                    if (name.contains(getResources().getString(R.string.mobile)))
                    {
                        mobileColumnNumber = i;
                    }
                }

            }
            for (int i = 0; i < Rows; i++) {  // 注意：这里是按行读取的！！！
                String name = sheet.getCell(0, i).getContents(); //默认第一列是联系人姓名
                String phone = sheet.getCell(phoneColumnNumber, i).getContents();//默认第一列是联系人电话号码
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
                getContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    // 仿微信弹框
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
        mPopupWindowList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPopupWindowList.hide();
                mSelectedItemView.setBackgroundColor(Color.WHITE);
                view.setBackgroundColor(Color.BLACK);
                if (position == ITEM_IMPORT_CONTACTS) {
                    try {
                        File file = new File(mSelectedItemFilePath);
                        getXlsFileDataByJxl(file);
                        new ImportContactsAsyncTask().execute();
                    } catch (Exception e) {
                    }
                }

            }
        });
        mPopupWindowList.show();
        mPopupWindowList.setOnWindowDismissListener(new OnWindowDismissListener() {
            @Override
            public void onWindowDismiss() {
                mSelectedItemView.setBackgroundColor(Color.WHITE);
            }
        });
    }

    //Android获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent(String param) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }
}
