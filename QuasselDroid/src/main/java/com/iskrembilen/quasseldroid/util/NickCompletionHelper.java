/*
    QuasselDroid - Quassel client for Android
    Copyright (C) 2015 Ken Børge Viktil
    Copyright (C) 2015 Magnus Fjell
    Copyright (C) 2015 Martin Sandsmark <martin.sandsmark@kde.org>

    This program is free software: you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation, either version 3 of the License, or (at your option)
    any later version, or under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License and the
    GNU Lesser General Public License along with this program.  If not, see
    <http://www.gnu.org/licenses/>.
 */

package com.iskrembilen.quasseldroid.util;

import android.widget.EditText;
import android.widget.Filter;

import com.iskrembilen.quasseldroid.protocol.state.IrcUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NickCompletionHelper extends Filter {

    private List<IrcUser> users;
    private int nickStart = 0;
    private EditText inputField;

    private String lastStringResult = null;
    private FilterResults lastFilteredResults = null;
    private int lastFilteredIndex = 0;

    public NickCompletionHelper(ArrayList<IrcUser> userList) {
        users = userList;
    }

    @SuppressWarnings("unchecked")
    public void completeNick(EditText inputField) {
        String inputText = inputField.getText().toString();
        if (lastStringResult != null && inputText.equals(lastStringResult)) {
            if (lastFilteredResults.count > lastFilteredIndex + 1) {
                lastFilteredIndex += 1;
                setNewContent(inputField, ((List<IrcUser>) lastFilteredResults.values).get(lastFilteredIndex).nick);
            } else if (lastFilteredResults.count > 0) {
                System.out.println("yes");
                lastFilteredIndex = 0;
                setNewContent(inputField, ((List<IrcUser>) lastFilteredResults.values).get(lastFilteredIndex).nick);
            }
            return;
        }
        this.inputField = inputField;
        String[] inputWords = inputText.split(" ");
        String inputNick = inputWords[inputWords.length - 1];
        nickStart = inputText.lastIndexOf(" ") == -1 ? 0 : inputText.substring(0, inputText.lastIndexOf(" ")).length();
        filter(inputNick);
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        List<IrcUser> filteredUsers = new ArrayList<IrcUser>();
        for (IrcUser user : users) {
            if (user.nick.toLowerCase(Locale.US).startsWith(((String) constraint).toLowerCase(Locale.US))) {
                filteredUsers.add(user);
            }
        }
        FilterResults filterResults = new FilterResults();
        filterResults.values = filteredUsers;
        filterResults.count = filteredUsers.size();
        return filterResults;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        if (results.count > 0) {
            lastFilteredResults = results;
            lastFilteredIndex = 0;
            setNewContent(inputField, ((List<IrcUser>) results.values).get(lastFilteredIndex).nick);
        }
        inputField = null;
    }

    private void setNewContent(EditText input, String nick) {
        if (input == null) return;
        String newContent = input.getText().toString().substring(0, nickStart) + (nickStart == 0 ? "" : " ") + nick + ((nickStart == 0 ? ": " : " "));
        lastStringResult = newContent;
        input.setText(newContent);
        input.setSelection(newContent.length());

    }

}
