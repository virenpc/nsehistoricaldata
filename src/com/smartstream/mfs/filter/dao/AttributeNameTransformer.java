package com.smartstream.mfs.filter.dao;

public interface AttributeNameTransformer {

	public String transformAttrName(String originalName);

	public String validateSqlColumnName(String sqlColumnName);
	
}
