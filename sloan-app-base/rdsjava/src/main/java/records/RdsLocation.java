package records;

public record RdsLocation(
	String location,
	String alias,
	String barcode,
	String area,
	String aisle,
	String bay,
	String row,
	String column,
	String neighborhood,
	String checkDigits
){}
