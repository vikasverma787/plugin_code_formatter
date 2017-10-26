package org.eclipse.examples.helloworld;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Formatter {

	static final String IF = "if";
	static final String FOR = "for";
	static final String BODY_START = "{";
	static final String BODY_END = "}";
	static final String TAB_SPACE = "\\t";
	static final String WHITESPACE_REGEX_ONE_OR_MORE = "\\s+";
	static final String WHITESPACE_REGEX_ZERO_OR_MORE = "\\s*";
	static final String IF_STATEMENT_MATCH = WHITESPACE_REGEX_ZERO_OR_MORE + IF + ".*\\" + BODY_START;
	static final String FOR_STATEMENT_MATCH = WHITESPACE_REGEX_ZERO_OR_MORE + FOR + ".*\\" + BODY_START;
	static final String NEW_LINE = "\n";
	static final String ASSIGNMENT_OPERATOR = ":=";

	public static void main(String[] args) {
		// "C:/temp/a.txt";//
		String strPath = "C:/Users/xvervik/workspace/plugin/src/a.txt";
		int tabToSpace = 4;
		if (args.length > 0 && Objects.nonNull(args[0])) {
			String tabSpaces = args[0];
			try {
				tabToSpace = Integer.parseInt(tabSpaces);
			} catch (NumberFormatException nex) {
				// proceed with default config of 1Tab = 4 Spaces
			}
		}

		Formatter.run(strPath, tabToSpace);

	}

	static class Index {
		int beginIndex;
		int endIndex;
		int maxSpaceBefore;
		int maxSpaceAfter;

		void reset() {
			beginIndex += endIndex + 1;
			endIndex = 0;
			maxSpaceBefore = 0;
			maxSpaceAfter = 0;
		}
	}

	static String generateBlankSpaceString(int num) {
		StringBuilder str = new StringBuilder();
		int whiteSpaces = num;
		while (whiteSpaces-- > 0) {
			str.append(" ");
		}
		return str.toString();
	}

	static boolean isAssignment(String str) {
		return Objects.nonNull(str) && !"".equals(str.trim()) && str.indexOf("=") != -1;
	}

	static void alignTheStatements(List<String> lstLines, Index index, String operator) {

		if (index.endIndex == 0)
			return;

		int indx = index.beginIndex;
		while (indx < lstLines.size()) {
			String strLine = lstLines.remove(indx);
			int currPos = strLine.indexOf(operator);
			strLine = strLine.replace(operator,
					generateBlankSpaceString(index.maxSpaceBefore - currPos + 1) + operator + " ");
			lstLines.add(indx, strLine);

			indx++;
		}
	}

	static UnaryOperator<String> formatStatement(String statementRegEx, String keywordRegEx) {
		return (str) -> {
			return str.matches(statementRegEx) ? str.replace(BODY_START,
					NEW_LINE + generateBlankSpaceString(str.indexOf(keywordRegEx)) + BODY_START) : str;
		};
	}

	static void run(String strPath, int tabToSpace) {

		final String tabToSpaceString = generateBlankSpaceString(tabToSpace);

		try (Stream<String> streamLines = Files.lines(Paths.get(strPath))) {

			Stream<String> strIfStatementsFormatted = streamLines
					.map(formatStatement(IF_STATEMENT_MATCH, IF).andThen(formatStatement(FOR_STATEMENT_MATCH, FOR)));

			Index index = new Index();

			List<String> output = strIfStatementsFormatted.reduce(new ArrayList<>(), (list, str) -> {

				str = str.replaceAll(TAB_SPACE, tabToSpaceString);

				str = str.replaceAll(WHITESPACE_REGEX_ONE_OR_MORE + ASSIGNMENT_OPERATOR + WHITESPACE_REGEX_ONE_OR_MORE,
						ASSIGNMENT_OPERATOR);

				
				//Remove white space
				str = removeWhiteSpace(str);
				
				//System.out.println(str);
				//Function parameter alignment
				//if(isFunction(strPath)

				if (isAssignment(str)) {
					// calculate the index of =
					index.maxSpaceBefore = Integer.max(index.maxSpaceBefore, str.indexOf(ASSIGNMENT_OPERATOR));
					index.endIndex++;

				} else {

					// realign the existing statements so far
					alignTheStatements(list, index, ASSIGNMENT_OPERATOR);
					index.reset();

					// otherwise its just another statement with no alignment
					// data, hence just accumulate without any processing
					// index.beginIndex++;

				}

				list.add(str);
				return list;
			}, (list1, list2) -> {
				list1.addAll(list2);

				return list1;
			});

			// align the statements if left
			alignTheStatements(output, index, ASSIGNMENT_OPERATOR);

			if (!output.isEmpty()) {
				try (FileWriter writer = new FileWriter(new File(strPath))) {
					String outputStr = output.stream().collect(Collectors.joining("\n"));
					//System.out.println(outputStr + "::" + outputStr.length());
					
					//outputStr = removeWhiteSpace(outputStr);
					
					//outputStr = getMethodArgs(outputStr);
					//System.out.println(outputStr + "::" + outputStr.length());
					writer.write(outputStr);
					writer.flush();
				}
			}

			// output.forEach(System.out::println);

		} catch (IOException iex) {
			iex.printStackTrace();
			System.err.println("Error while trying to perform operation on the file at path : " + strPath);
		}

	}

	static String removeWhiteSpace(String line) {
		return line.replaceAll("\\s+$", "");
	}

}
