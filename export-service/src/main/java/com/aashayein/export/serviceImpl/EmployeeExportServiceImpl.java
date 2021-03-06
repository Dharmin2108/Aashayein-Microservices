/**
 * @ProjectName: export-service
 * @PackageName: com.aashayein.export.serviceImpl
 * @FileName: EmployeeExportServiceImpl.java
 * @Author: Avishek Das
 * @CreatedDate: 23-06-2019
 * @Modified_By avishek.das @Last_On 23-Jun-2019 10:54:10 PM
 */

package com.aashayein.export.serviceImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.aashayein.export.dto.EmployeeTO;
import com.aashayein.export.dto.ExcelDetails;
import com.aashayein.export.service.EmployeeExportService;
import com.aashayein.export.service.EmployeeService;
import com.aashayein.export.util.ExcelWriter;

import lombok.extern.slf4j.Slf4j;

@Service
@RefreshScope
@Slf4j
public class EmployeeExportServiceImpl implements EmployeeExportService {

	@Autowired
	private ExcelWriter excelWriter;

	@Autowired
	private EmployeeService employeeService;

	@Value("${directory.temp}")
	private String tempDirectory;

	@Override
	public String exportEmployeesToExcel() throws Exception {

		String fileLocation = null;
		String fileName = "employees.xlsx";
		String directoryLocation = "";

		// Get all employees details
		List<EmployeeTO> employees = employeeService.getEmployees();

		// Convert List<EmployeeTO> to List<Object>
		List<Object> employeeList = new ArrayList<Object>();
		employees.stream().forEach(employee -> employeeList.add((Object) employee));

		// Excel sheet columns
		String[] columns = { "Employee Code", "Name", "Gender", "Mobile No.", "Email Id", "Job Title", "Role",
				"Joining Date", "Archived", "Country", "State", "City", "PinCode", "Address" };

		// header style
		Map<String, String> headerStyle = new HashedMap<String, String>();
		headerStyle.put("font", "Arial");
		headerStyle.put("bold", "true");
		headerStyle.put("color", "BLACK");
		headerStyle.put("alignment", "CENTER");
		headerStyle.put("verticalAlignment", "CENTER");
		headerStyle.put("bottomBorder", "THIN");
		headerStyle.put("bottomBorderColor", "GREEN");

		// Record Not Found Style
		Map<String, String> recordNotFoundStyle = new HashedMap<String, String>();
		recordNotFoundStyle.put("font", "Arial");
		recordNotFoundStyle.put("bold", "true");
		recordNotFoundStyle.put("color", "RED");
		recordNotFoundStyle.put("alignment", "CENTER");
		recordNotFoundStyle.put("verticalAlignment", "CENTER");

		// Content Style
		Map<String, String> contentStyle = new HashedMap<String, String>();
		contentStyle.put("font", "Arial");
		contentStyle.put("color", "BLACK");
		contentStyle.put("alignment", "CENTER");
		contentStyle.put("verticalAlignment", "CENTER");

		// Date Cell Style
		Map<String, String> dateCellStyle = new HashedMap<String, String>();
		dateCellStyle.put("font", "Arial");
		dateCellStyle.put("color", "BLACK");
		dateCellStyle.put("alignment", "CENTER");
		dateCellStyle.put("verticalAlignment", "CENTER");
		dateCellStyle.put("dataFormat", "dd/MM/yyyy");

		// 7 no column should have the dateCellStyle
		Map<Integer, Map<String, String>> specificCellStyle = new HashedMap<Integer, Map<String, String>>();
		specificCellStyle.put(7, dateCellStyle);

		// Setting export excel details
		ExcelDetails excelDetails = new ExcelDetails();

		excelDetails.setSheetName("Employees");
		excelDetails.setSpreadSheetMethodName("buildSheetForEmployees");
		excelDetails.setColumns(columns);
		excelDetails.setHeaderStyle(headerStyle);
		excelDetails.setData(employeeList);
		excelDetails.setRecordNotFoundStyle(recordNotFoundStyle);
		excelDetails.setContentStyle(contentStyle);
		excelDetails.setSpecificCellStyle(specificCellStyle);

		// Export the employee excel sheet
		XSSFWorkbook workbook = excelWriter.buildWorkbook(excelDetails);

		FileOutputStream outputStream = null;
		try {
			File currDir = new File(".");

			directoryLocation = currDir.getAbsolutePath().substring(0, currDir.getAbsolutePath().length() - 1)
					+ tempDirectory;
			File uploadDirectory = new File(directoryLocation);

			// If uploadDirectory not exists, create the directory
			if (!uploadDirectory.exists()) {
				uploadDirectory.mkdir();
			}

			fileLocation = directoryLocation + fileName;

			outputStream = new FileOutputStream(fileLocation);
			workbook.write(outputStream);

			log.info("SpreedSheet Saved, Path : " + fileLocation);
		} catch (IOException ioe) {
			throw new RuntimeException("Error writing spreadsheet to output stream");
		} finally {
			if (workbook != null) {
				workbook.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
		}

		return fileLocation;
	}
}
