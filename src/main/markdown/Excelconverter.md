Excelconverter is written to solve a specific problem:

- Systembolaget provide excel files to writers with information on beers for release
- The writers/bloggers rewrite that information as text, which takes time and is prone  
to errors.

Excelconverter solves the problem by accepting an excel file for upload, then converting   
the content first by sheet, then by row to a blog friendly template.

The converter does not provide any access control, you will have to do that by placing a web server in front of the service.

The converter can be run as-is, or as a docker or k8s container.

After building the project, the service can be accessed by browsing to:

*http://localhost:8080/pilsner/excel/upload.html*

