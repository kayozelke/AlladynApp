from datetime import datetime
import json
import fpdf

from fpdf import FPDF

class PDF(FPDF):
    def __init__(self, orientation='P', unit='mm', format='A4'):
        super().__init__(orientation, unit, format)
        self.set_auto_page_break(auto=True, margin=15)

    def header(self):
        # Custom header for the PDF, if needed
        self.set_font("Arial", 'B', 17)
        self.cell(0, 10, 'Raport', 0, 1, 'C')

    def footer(self):
        # Custom footer for the PDF, if needed
        self.set_y(-15)
        self.set_font("Arial", 'I', 8)
        # self.cell(0, 10, f'Page {self.page_no()}', 0, 0, 'C')


def getTimestampNow():
    return (datetime.now().timestamp())

def loadCarpets(input_jsn_file_path : str):
    with open(input_jsn_file_path, 'r', encoding="utf-8") as r_file:
        return json.load(r_file)
    #     loaded_jsn = json.load(r_file)
    
    # return loaded_jsn.get('carpets',{})


def filterCarpetsDatesRange(loaded_jsn: dict, start_time: int, end_time: int):
    filteredData = {}
    
    for key,value in loaded_jsn.items():
        print(int(key)/1000, ">", end_time)
        # time is in miliseconds
        if int(key)/1000 >= start_time and int(key)/1000 <= end_time:
            filteredData[key] = value
    
    return filteredData
    
    
    
def filterCarpetsCompany(loaded_jsn:dict):
    filteredData = {}
    
    for key,value in loaded_jsn.items():
        print(value.get('ifCompanyPartner', None))
        if value.get('ifCompanyPartner', None) == True:
            filteredData[key] = value
    
    return filteredData

def filterCarpetsPrivate(loaded_jsn:dict):
    filteredData = {}
    
    for key,value in loaded_jsn.items():
        if value.get('ifPrivateClient', None) == True:
            filteredData[key] = value
    
    return filteredData

def fixPrice(number):
    
    return "{:.2f}".format( float(number) )

def fixCarpetsPrices(loaded_jsn: dict):
    # save price as 123.45
    # fix for ints and wrong floats
    for key,value in loaded_jsn.items():
        if value.get('price', None) != None:
            # loaded_jsn[key]['price'] = "{:.2f}".format( float(value['price']) )
            loaded_jsn[key]['price'] = fixPrice(value['price'])
                
    return loaded_jsn

def writeDataToPdf(data : dict, header_map : dict, output_pdf_file_path : str, summary_headers_ids = []):
    # Calculate the required height and width for the table
    cell_width = 30
    cell_height = 10
    num_columns = len(header_map.keys())
    num_rows = len(data.keys()) + 1  # Including header row

    page_width = cell_width * num_columns + 20  # Adding some margin
    page_height = cell_height * num_rows + 20  # Adding some margin

    # Initialize PDF with custom dimensions
    # pdf = PDF(orientation='P', unit='mm', format=(page_width, page_height))
    pdf = PDF(orientation='P', unit='mm', format=(page_width, 300))
    pdf.add_page()
    pdf.set_font("Arial", size=12)

    # Add table header
    pdf.set_font("Arial", 'B', size=10)
    for header_key, header_val in header_map.items():
        # key is for program only
        # value is columns name text put to PDF
        pdf.cell(cell_width, cell_height, header_val, 1, 0, 'C')
    pdf.ln()

    # Add table rows
    pdf.set_font("Arial", size=8)
    
    for data_key, data_val in data.items():
        for header_key, header_val in header_map.items():
            pdf.cell(cell_width, cell_height, str(data_val.get(header_key)), 1, 0, 'C')
        pdf.ln()
        
        
        
    # add summary
    summary_cell_w = cell_width + 10
    summary_cell_h = cell_height
    
    pdf.set_xy(10, pdf.get_y() + 10)
    pdf.set_font("Arial", 'B', size=10)
    # Define table headers
    for header in summary_headers_ids:
        pdf.cell(summary_cell_w, summary_cell_h, f"Suma - \n{header_map.get(header)}", 1, 0, 'C')
    pdf.ln()
    
    # Add final summary table rows
    pdf.set_font("Arial", size=8)
    
    sums = {}
    
    for header in summary_headers_ids:
        sums[header] = 0
        for data_key, data_val in data.items():
            sums[header] += float(fixPrice(data_val.get(header)))
        pdf.cell(summary_cell_w, summary_cell_h, fixPrice(sums[header]), 1, 0, 'C')
    pdf.ln()
            
    

    # Save the PDF to a file
    pdf.output(output_pdf_file_path, 'F')
    

def generatePdfSummary(
    input_jsn_file_path : str, output_pdf_file_path: str, 
    start_timestamp_seconds: int, end_timestamp_seconds: int, 
    # by the default only company partners will be written
    private_partners = False
    ):
    carpetsData = loadCarpets(input_jsn_file_path)
    
    # date
    carpetsData = filterCarpetsDatesRange(carpetsData, start_timestamp_seconds, end_timestamp_seconds)
    # type of job
    if not private_partners : carpetsData = filterCarpetsCompany(carpetsData)
    else : carpetsData = filterCarpetsPrivate(carpetsData)
    
    # fix prices
    carpetsData = fixCarpetsPrices(loaded_jsn=carpetsData)
    
    print(carpetsData.keys())
    # return
    
    print("PDF generating...")
    
    '''
        "carpetNumber": "", // tylko dla partner
        "date": "2024-06-12 16:45:32", // all
        "ifCompanyPartner": false, // --
        "ifPrivateClient": true, // --
        "length": 1.1, // all
        "metricArea": 0.8, // all
        "ownerPhoneNumber": "123123123", //tylko dla private
        "ownerSurname": "Autoniak", // tylko dla private
        "pickUpPoint": "", // tylko dla partner
        "price": 10.4, // all
        "width": 0.7 // all
    '''
    
    if not private_partners:
        column_map = {
            "carpetNumber" : 'Numer', 
            "pickUpPoint" : 'Punkt odbioru', 
            "date" : 'Data', 
            "length" : 'Dlugosc [m]', 
            "width": 'Szerokosc [m]', 
            "metricArea" : 'Rozmiar [m^2]', 
            "price" : 'Cena [PLN]'
        }
    else:
        column_map = {
            # "carpetNumber" : 'Numer', 
            "date" : 'Data', 
            "length" : 'Dlugosc [m]', 
            "width": 'Szerokosc [m]', 
            "metricArea" : 'Rozmiar [m^2]', 
            "ownerPhoneNumber" : "Telefon",
            "ownerSurname" : "Nazwisko",
            # "pickUpPoint" : 'Punkt odbioru', 
            "price" : 'Cena [PLN]'
        }
        
    summary_headers = ['metricArea', 'price']
    
    writeDataToPdf(carpetsData, column_map, output_pdf_file_path, summary_headers)
    

    
    
    return



# demo

# FILE = "alladyn-91bcc-default-rtdb-export.json"

# generatePdfSummary(FILE, "test.pdf", 1, getTimestampNow(), private_partners=True)
# generatePdfSummary(FILE, "test.pdf", 1, getTimestampNow(), private_partners=False)

