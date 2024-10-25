import tkinter as tk
import tkinter.messagebox as messagebox
from tkinter import ttk
from googletrans import Translator, LANGUAGES

# Функция для копирования текста
def copy_text(event=None):
    try:
        selected_text = text_input.selection_get()
        root.clipboard_clear()
        root.clipboard_append(selected_text)
    except tk.TclError:
        pass

# Функция для вставки текста
def paste_text(event=None):
    try:
        clipboard_text = root.clipboard_get()
        text_input.insert(tk.INSERT, clipboard_text)
    except tk.TclError:
        pass

# Функция для перевода текста
def translate_text():
    translator = Translator()
    source_text = text_input.get("1.0", tk.END).strip()
    src_lang = source_lang_combo.get()
    dest_lang = dest_lang_combo.get()

    if source_text == "":
        translated_text.delete("1.0", tk.END)  # Очистка поля перевода
        return
    
    try:
        translation = translator.translate(source_text, src=src_lang, dest=dest_lang)
        translated_text.delete("1.0", tk.END)  # Очистка поля перевода
        translated_text.insert(tk.END, translation.text)  # Вставка переведенного текста
    except Exception as e:
        messagebox.showerror("Ошибка", "Ошибка перевода: " + str(e))

def replace_input_text(new_text):
    text_input.delete("1.0", tk.END)
    text_input.insert(tk.END, new_text)

# Функция для обратного перевода
def reverse_translate():
    # Получаем текст из переведенного поля
    text_to_reverse = translated_text.get("1.0", tk.END).strip()
    replace_input_text(text_to_reverse)
    
    src_lang = source_lang_combo.get()
    dest_lang = dest_lang_combo.get()
    
    source_lang_combo.set(dest_lang)
    dest_lang_combo.set(src_lang)
    
    translate_text()

# Создание окна
root = tk.Tk()
root.title("Переводчик")
root.geometry("800x600")  # Увеличение размера окна
root.resizable(False, False)

# Заголовок
title_label = ttk.Label(root, text="Терджиман", font=("Helvetica", 16))
title_label.pack(pady=10)

# Фрейм для ввода текста и его прокрутки
input_frame = ttk.Frame(root)
input_frame.pack(pady=10, padx=10, fill=tk.BOTH, expand=True)

text_input = tk.Text(input_frame, height=10, wrap=tk.WORD)
text_input.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

# Полоса прокрутки для поля ввода
input_scroll = ttk.Scrollbar(input_frame, orient="vertical", command=text_input.yview)
input_scroll.pack(side=tk.RIGHT, fill=tk.Y)
text_input['yscrollcommand'] = input_scroll.set

# Создание выпадающих списков для выбора языков
languages = list(LANGUAGES.values())

src_lang_label = ttk.Label(root, text="Исходный язык:")
src_lang_label.pack(pady=5)
source_lang_combo = ttk.Combobox(root, values=languages, width=30, state="readonly")
source_lang_combo.set("russian")
source_lang_combo.pack()

dest_lang_label = ttk.Label(root, text="Язык перевода:")
dest_lang_label.pack(pady=5)
dest_lang_combo = ttk.Combobox(root, values=languages, width=30, state="readonly")
dest_lang_combo.set("english")
dest_lang_combo.pack()

# Кнопка для перевода
translate_button = ttk.Button(root, text="Перевести", command=translate_text)
translate_button.pack(pady=10)

# Кнопка для обратного перевода
reverse_button = ttk.Button(root, text="Обратный перевод", command=reverse_translate)
reverse_button.pack(pady=10)

# Фрейм для вывода переведенного текста и его прокрутки
output_frame = ttk.Frame(root)
output_frame.pack(pady=10, padx=10, fill=tk.BOTH, expand=True)

# Поле для вывода переведенного текста
translated_text = tk.Text(output_frame, height=10, wrap=tk.WORD)
translated_text.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

# Полоса прокрутки для переведенного текста
output_scroll = ttk.Scrollbar(output_frame, orient="vertical", command=translated_text.yview)
output_scroll.pack(side=tk.RIGHT, fill=tk.Y)
translated_text['yscrollcommand'] = output_scroll.set

# Запуск основного цикла приложения
root.mainloop()
