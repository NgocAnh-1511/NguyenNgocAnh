
dmy=input("Nhập ngày tháng năm theo định dạng dd/mm/yyyy: ")
day=int(dmy[0:2])
month=int(dmy[3:5])
year=int(dmy[6:])
if month > 0 and month <= 12:
    if year % 400 == 0 or (year % 4 == 0 and year % 100 != 0):  # Kiểm tra năm nhuận
        if month == 2:
            if day < 1 or day > 29:
                print("Ngày không hợp lệ")
            else:
                print("Ngày tháng năm hợp lệ")
        else:
            if month in [4, 6, 9, 11]:
                if day < 1 or day > 30:
                    print("Ngày không hợp lệ")
                else:
                    print("Ngày tháng năm hợp lệ")
            else:
                if day < 1 or day > 31:
                    print("Ngày không hợp lệ")
                else:
                    print("Ngày tháng năm hợp lệ")
    else:  # Năm không nhuận
        if month == 2:
            if day < 1 or day > 28:
                print("Ngày không hợp lệ")
            else:
                print("Ngày tháng năm hợp lệ")
        else:
            if month in [4, 6, 9, 11]:
                if day < 1 or day > 30:
                    print("Ngày không hợp lệ")
                else:
                    print("Ngày tháng năm hợp lệ")
            else:
                if day < 1 or day > 31:
                    print("Ngày không hợp lệ")
                else:
                    print("Ngày tháng năm hợp lệ")
else:
    print("Tháng không hợp lệ")
