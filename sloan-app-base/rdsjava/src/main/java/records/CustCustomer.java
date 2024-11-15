package records;

public record CustCustomer(
		String customerNumber,
		String customerName,
		String addressLine1,
		String addressLine2,
		String addressLine3,
		String city,
		String state,
		String zipcode,
		String exportFlag
){}