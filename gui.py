import glob
import json
import tarfile
from tkinter import *
from tkinter import filedialog as fd
from tkinter import ttk
from tkinter.font import Font
import requests
import audit
import re
global previous

main = Tk()
myFont = Font(family="Mincho", size=13)
s = ttk.Style()
s.configure('TFrame', background='#FFC7FF')
main.title("Security Benchmarking Tool Lab 2 Malîi Antonela")
main.geometry("1550x700")
frame = ttk.Frame(main, width=1550, height=700, style='TFrame', padding=(4, 4, 450, 450))
frame.grid(column=0, row=0)

previous = []
index = 0
arr = []
matching = []
querry =StringVar()
vars = StringVar()
tofile = []
structure = []


def entersearch(evt):
    search()


def search():
    global structure
    q = querry.get()
    arr = [struct['description'] for struct in structure if q.lower() in struct['description'].lower()]
    global matching
    matching = [struct for struct in structure if q in struct['description']]
    vars.set(arr)


def on_select_configuration(evt):
    global previous
    global index
    w = evt.widget
    actual = w.curselection()

    difference = [item for item in actual if item not in previous]
    if len(difference) > 0:
        index = [item for item in actual if item not in previous][0]
    previous = w.curselection()

    text.delete(1.0, END)
    str = '\n'
    for key in matching[index]:
        str += key + ':' + matching[index][key] + '\n'
    text.insert(END, str)


def download_url(url, save_path, chunk_size=1024):
    r = requests.get(url, stream=True)
    with open(save_path, 'wb') as fd:
        for chunk in r.iter_content(chunk_size=chunk_size):
            fd.write(chunk)


def extract_download():
    url = "https://www.tenable.com/downloads/api/v1/public/pages/download-all-compliance-audit-files/downloads/7472/download?i_agree_to_tenable_license_agreement=true"
    path = "audits.tar.gz"
    download_url(url, path)
    tf = tarfile.open("audits.tar.gz")
    tf.extractall()
    print(glob.glob("portal_audits/*"))


def import_audit():
    global arr
    file_name = fd.askopenfilename(initialdir="../portal_audits")
    if file_name:
        arr = []
    global structure
    structure = audit.main(file_name)
    for element in structure:
        for key in element:
            str = ''
            for char in element[key]:
                if char != '"' and char != "'":
                    str += char
            isspacefirst = True
            str2 = ''
            for char in str:
                if char == ' ' and isspacefirst:
                    continue
                else:
                    str2 += char
                    isspacefirst = False
            element[key] = str2

    global matching
    matching = structure
    if len(structure) == 0:
        f = open(file_name, 'r')
        structure = json.loads(f.read())
        f.close()
    for struct in structure:
        if 'description' in struct:
            arr.append(struct['description'])
        else:
            arr.append('Error in selecting')
    vars.set(arr)

#highlighting the specifications of each audit file



lstbox = Listbox(frame, bg="#000000", font=myFont, fg="white", listvariable=vars, selectmode=MULTIPLE, width=95,
                 height=25, highlightthickness=3)
lstbox.config(highlightbackground="white")
lstbox.grid(row=0, column=0, columnspan=3, padx=50, pady=105)
lstbox.bind('<<ListboxSelect>>', on_select_configuration)


def save_config():
    file_name = fd.asksaveasfilename(filetypes=(("Audit FILES", ".audit"),
                                                ("All files", ".")))
    file_name += '.audit'
    file = open(file_name, 'w')
    selection = lstbox.curselection()
    for i in selection:
        tofile.append(matching[i])
    json.dump(tofile, file)
    file.close()


def select_all():
    lstbox.select_set(0, END)
    for struct in structure:
        lstbox.insert(END, struct)


def deselect_all():
    for struct in structure:
        lstbox.selection_clear(0, END)




text = Text(frame, bg="#000000", fg="white", font=myFont, width=55, height=27, highlightthickness=3)
text.config(highlightbackground="white")
text.grid(row=0, column=3, columnspan=3, padx=40)
saveButton = Button(frame, bg="#0023FF", fg="white", font=myFont, text="Save", width=7, height=1,
                    command=save_config).place(relx=0.01, rely=0.089)
import_button = Button(frame, bg="#0023FF", fg="white", font=myFont, text="Import", width=7, height=1,
                       command=import_audit).place(relx=0.09, rely=0.089)
downloadButton = Button(frame, bg="#0023FF", fg="white", font=myFont, text="Download audits", width=15, height=1,
                        command=extract_download).place(relx=0.17, rely=0.089)
selectAllButton = Button(frame, bg="#0023FF", fg="white", font=myFont, text="Select All", width=7, height=1,
                         command=select_all).place(relx=0.01, rely=0.859)
deselectAllButton = Button(frame, bg="#0023FF", fg="white", font=myFont, text="Deselect All", width=10, height=1,
                           command=deselect_all).place(relx=0.09, rely=0.859)
global e
e = Entry(frame, bg="#f5f5f5", font=myFont, width=30, textvariable=querry).place(relx=0.29, rely=0.095)
search_button = Button(frame, bg="#0023FF", fg="white", font=myFont, text="Search", width=7, height=1,
                       command=search).place(relx=0.48, rely=0.089)

main.bind('<Return>', entersearch)
main.mainloop()