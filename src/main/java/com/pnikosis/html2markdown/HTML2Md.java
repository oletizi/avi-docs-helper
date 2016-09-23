package com.pnikosis.html2markdown;

import com.pnikosis.html2markdown.MDLine.MDLineType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Convert Html to MarkDown
 */
public class HTML2Md {
  private static int indentation = -1;
  private static int listDepth = 0;
  private static boolean orderedList = false;

  public static String convert(String theHTML, String baseURL) {
    Document doc = Jsoup.parse(theHTML, baseURL);

    return parseDocument(doc);
  }

  public static String convert(URL url, int timeoutMillis) throws IOException {
    Document doc = Jsoup.parse(url, timeoutMillis);

    return parseDocument(doc);
  }

  public static String convertHtml(String html, String charset) throws IOException {
    Document doc = Jsoup.parse(html, charset);

    return parseDocument(doc);
  }

  public static String convertFile(File file, String charset) throws IOException {
    Document doc = Jsoup.parse(file, charset);

    return parseDocument(doc);
  }

  public static void htmlToJekyllMd(String htmlPath, String mdPath, String charset) {
    try {
      List<File> fileList = FilesUtil.getAllFiles(htmlPath, "html");
      for (File file : fileList) {
        String mdName = file.getAbsolutePath().replace(htmlPath, mdPath).replace("html", "md");
        String hmPath = mdName.substring(0, mdName.lastIndexOf("/")) + "/";
        String separator = System.getProperty("line.separator");
        String head = "---" + separator +
            "layout: post" + separator +
            "title: \"" + file.getName() + "\"" + separator +
            "description: \"" + file.getName() + "\"" + separator +
            "category: pages\"" + separator +
            "tags: [blog]\"" + separator +
            "--- " + separator +
            "{% include JB/setup %}" + separator
            + separator;
        FilesUtil.isExist(hmPath);
        String parsedText = convertFile(file, charset);
        Calendar calendar = Calendar.getInstance();
        String dateName = DateUtil.dateToShortString(calendar.getTime());
        String newName = dateName + "-" + hmPath.replace(mdPath, "").replace("/", "-") + "-" + file.getName();
        String mmName = (hmPath + newName.replace("html", "md")).replaceAll("\\s*", "");
        FilesUtil.newFile(mmName, head + parsedText, charset);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void htmlToHexoMd(String htmlPath, String mdPath, String charset) {
    try {
      List<File> fileList = FilesUtil.getAllFiles(htmlPath, "html");
      for (File file : fileList) {
        String mdName = file.getAbsolutePath().replace(htmlPath, mdPath).replace("html", "md");
        String hmPath = mdName.substring(0, mdName.lastIndexOf("/")) + "/";
        String separator = System.getProperty("line.separator");
        String[] strings = hmPath.replace(mdPath, "").split("/");
        Calendar calendar = Calendar.getInstance();
        String dateName = DateUtil.dateToShortString(calendar.getTime());
        String dateString = DateUtil.dateToLongString(calendar.getTime());
        StringBuilder blog = new StringBuilder();
        StringBuilder categories = new StringBuilder();
        Map<String, String> stringMap = new TreeMap<String, String>();
        for (String value : strings) {
          stringMap.put(value, value);
        }
        for (String tag : stringMap.keySet()) {
          blog.append(" - ").append(tag).append(separator);
        }
        categories.append(strings[0]);
        String head = "---" + separator +
            "layout: post" + separator +
            "title: \"" + file.getName().replace(".html", "").split("-")[0] + "\"" + separator +
            "date: " + dateString + separator +
            "categories: " + categories + separator +
            "tags: " + separator +
            blog.toString() +
            "--- " + separator +
            separator;
        FilesUtil.isExist(hmPath);
        String parsedText = HTML2Md.convertFile(file, "utf-8");
        String newName = dateName + "-" + hmPath.replace(mdPath, "").replace("/", "-") + "-" + file.getName();
        String mmName = (hmPath + newName.replace("html", "md")).replaceAll("\\s*", "");
        FilesUtil.newFile(mmName, head + parsedText, charset);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String parseDocument(Document dirtyDoc) {
    indentation = -1;

    return getTextContent(dirtyDoc);
  }

  private static String getTextContent(Element element) {
    ArrayList<MDLine> lines = new ArrayList<>();

    List<Node> children = element.childNodes();
    for (Node child : children) {
      if (child instanceof TextNode) {
        TextNode textNode = (TextNode) child;
        MDLine line = getLastLine(lines);
        if (line.getContent().equals("")) {
          if (!textNode.isBlank()) {
            line.append(textNode.text().replaceAll("#", "/#").replaceAll("\\*", "/\\*"));
          }
        } else {
          line.append(textNode.text().replaceAll("#", "/#").replaceAll("\\*", "/\\*"));
        }

      } else if (child instanceof Element) {
        Element childElement = (Element) child;
        processElement(childElement, lines);
      } else {
        //System.out.println();
      }
    }

    int blankLines = 0;
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i).toString();//.trim();
      if (line.trim().equals("")) {
        blankLines++;
      } else {
        blankLines = 0;
      }
      if (blankLines < 2) {
        result.append(line);
        if (i < lines.size() - 1) {
          result.append("\n");
        }
      }
    }

    return result.toString();
  }

  private static void processElement(Element element, ArrayList<MDLine> lines) {
    Tag tag = element.tag();

    String tagName = tag.getName();
    if (tagName.equals("div")) {
      div(element, lines);
    } else if (tagName.equals("blockquote")) {
      blockquote(element, lines);
    } else if (tagName.equals("p")) {
      p(element, lines);
    } else if (tagName.equals("br")) {
      br(lines);
    } else if (tagName.matches("^h[0-9]+$")) {
      h(element, lines);
    } else if (tagName.equals("strong") || tagName.equals("b")) {
      strong(element, lines);
    } else if (tagName.equals("em")) {
      em(element, lines);
    } else if (tagName.equals("hr")) {
      hr(lines);
    } else if (tagName.equals("a")) {
      a(element, lines);
    } else if (tagName.equals("img")) {
      img(element, lines);
    } else if (tagName.equals("pre")) {
      pre(element, lines);
    } else if (tagName.equals("code")) {
      code(element, lines);
    } else if (tagName.equals("ul")) {
      ul(element, lines);
    } else if (tagName.equals("ol")) {
      ol(element, lines);
    } else if (tagName.equals("li")) {
      li(element, lines);
    } else if (tagName.equals("table")) {
      table(element, lines);
    } else if (tagName.equals("thead")) {
      thead(element, lines);
    } else if (tagName.equals("tbody")) {
      tbody(element, lines);
    } else if (tagName.equals("tr")) {
      tr(element, lines);
    } else if (tagName.equals("th")) {
      th(element, lines);
    } else if (tagName.equals("td")) {
      td(element, lines);
    } else if (tagName.equals("figure")) {
      figure(element, lines);
    } else if (tagName.equals("figcaption")) {
      figcapture(element, lines);
    } else {
      MDLine line = getLastLine(lines);
      line.append(getTextContent(element));
    }
  }

  private static void th(Element element, ArrayList<MDLine> lines) {
    String attributes = "";
    if (element.hasAttr("rowspan")) {
      attributes += " rowspan=\"" + element.attr("rowspan") + "\"";
    }
    if (element.hasAttr("colspan")) {
      attributes += " colspan=\"" + element.attr("colspan") + "\"";
    }
    keepAndProcessChildren(element, lines, "<th" + attributes + ">", "</th>");
  }

  private static void tbody(Element element, ArrayList<MDLine> lines) {
    keepAndProcessChildren(element, lines, "<tbody>", "</tbody>");
  }

  private static void thead(Element element, ArrayList<MDLine> lines) {
    keepAndProcessChildren(element, lines, "<thead>", "</thead>");
  }

  private static void td(Element element, ArrayList<MDLine> lines) {
    String attributes = "";
    if (element.hasAttr("rowspan")) {
      attributes += " rowspan=\"" + element.attr("rowspan") + "\"";
    }
    if (element.hasAttr("colspan")) {
      attributes += " colspan=\"" + element.attr("colspan") + "\"";
    }
    //keepAndProcessChildren(element, lines, "<td" + attributes +">", "</td>");
    lines.add(new MDLine(MDLineType.None, indentation, "<td" + attributes + ">" + element.html() + "</td>"));
  }

  private static void tr(Element element, ArrayList<MDLine> lines) {
    keepAndProcessChildren(element, lines, "<tr>", "</tr>");
  }

  private static void keepAndProcessChildren(Element element, ArrayList<MDLine> lines, String openTag, String closeTag) {
    final MDLine line = new MDLine(MDLineType.None, indentation, openTag);
    lines.add(line);
    for (Node child : element.childNodes()) {
      if (child instanceof TextNode) {
        line.append(((TextNode) child).text());
      } else if (child instanceof Element) {
        processElement((Element) child, lines);
      } else {
        System.err.println("I don't know what to do with this node type: " + child);
      }
    }
    lines.add(new MDLine(MDLineType.None, indentation, closeTag));
  }

  private static void figcapture(Element element, ArrayList<MDLine> lines) {
    keepAndProcessChildren(element, lines, "<figcapture>", "</figcapture>");
  }

  private static void figure(Element element, ArrayList<MDLine> lines) {
    String clazz = element.attr("class");
    keepAndProcessChildren(element, lines, "<figure class=\"" + clazz + "\">", "</figure>");
  }

  private static void blockquote(Element element, ArrayList<MDLine> lines) {
    String text = element.html();
    text = text.replaceAll("<p>", "");
    text = text.replaceAll("</p>", "");
    text = text.replaceAll("<br>", "\n");
    text = Pattern.compile("^(.*)", Pattern.MULTILINE).matcher(text).replaceAll("> $1");
    getLastLine(lines).append(text);
  }

  private static void pre(Element element, ArrayList<MDLine> lines) {
    element.removeAttr("crayon");
    lines.add(new MDLine(MDLineType.None, listDepth, ""));
    passthrough(element, lines);
  }

  private static void table(Element element, ArrayList<MDLine> lines) {
    String clazz = element.attr("class");
    clazz += " table table-bordered table-hover";
    keepAndProcessChildren(element, lines, "<table class=\"" + clazz + "\">", "</table>");
  }

  private static void passthrough(Element element, ArrayList<MDLine> lines) {
    getLastLine(lines).append(element.outerHtml());
  }

  private static MDLine getLastLine(ArrayList<MDLine> lines) {
    MDLine line;
    if (lines.size() > 0) {
      line = lines.get(lines.size() - 1);
    } else {
      line = new MDLine(MDLineType.None, 0, "");
      lines.add(line);
    }

    return line;
  }

  private static void div(Element element, ArrayList<MDLine> lines) {
    MDLine line = getLastLine(lines);
    String content = getTextContent(element);
    if (!content.equals("")) {
      if (!line.getContent().trim().equals("")) {
        lines.add(new MDLine(MDLineType.None, indentation, ""));
        lines.add(new MDLine(MDLineType.None, indentation, content));
        lines.add(new MDLine(MDLineType.None, indentation, ""));
      } else {
        if (!content.trim().equals(""))
          line.append(content);
      }
    }
  }

  private static void p(Element element, ArrayList<MDLine> lines) {
    MDLine line = getLastLine(lines);
    if (!line.getContent().trim().equals(""))
      lines.add(new MDLine(MDLineType.None, listDepth, ""));
    lines.add(new MDLine(MDLineType.None, listDepth, ""));

    String textContent = getTextContent(element);
    lines.add(new MDLine(MDLineType.None, listDepth, textContent));
    lines.add(new MDLine(MDLineType.None, listDepth, ""));
    if (!line.getContent().trim().equals("")) {
      lines.add(new MDLine(MDLineType.None, listDepth, ""));
    }
  }

  private static void br(ArrayList<MDLine> lines) {
    MDLine line = getLastLine(lines);
    if (!line.getContent().trim().equals(""))
      lines.add(new MDLine(MDLineType.None, 0, ""));
  }

  private static void h(Element element, ArrayList<MDLine> lines) {
    if ("".equals(element.text().trim())) {
      // nothing in here
      return;
    }
    indentation = -1;
    MDLine line = getLastLine(lines);
    if (!line.getContent().trim().equals(""))
      lines.add(new MDLine(MDLineType.None, 0, ""));

    int level = Integer.valueOf(element.tagName().substring(1));
    switch (level) {
      case 1:
        lines.add(new MDLine(MDLineType.Head1, 0, getTextContent(element)));
        break;
      case 2:
        lines.add(new MDLine(MDLineType.Head2, 0, getTextContent(element)));
        break;
      default:
        lines.add(new MDLine(MDLineType.Head3, 0, getTextContent(element)));
        break;
    }

    lines.add(new MDLine(MDLineType.None, 0, ""));
    lines.add(new MDLine(MDLineType.None, 0, ""));
  }

  private static void strong(Element element, ArrayList<MDLine> lines) {
    MDLine line = getLastLine(lines);
    line.append("**");
    line.append(getTextContent(element));
    line.append("**");
  }

  private static void em(Element element, ArrayList<MDLine> lines) {
    MDLine line = getLastLine(lines);
    line.append("*");
    line.append(getTextContent(element));
    line.append("*");
  }

  private static void hr(ArrayList<MDLine> lines) {
    lines.add(new MDLine(MDLineType.None, 0, ""));
    lines.add(new MDLine(MDLineType.HR, 0, ""));
    lines.add(new MDLine(MDLineType.None, 0, ""));
  }

  private static void a(Element element, ArrayList<MDLine> lines) {
    MDLine line = getLastLine(lines);
    line.append(element.outerHtml());
  }

  private static void img(Element element, ArrayList<MDLine> lines) {
    MDLine line = getLastLine(lines);
    line.append(element.outerHtml());

  }

  private static void code(Element element, ArrayList<MDLine> lines) {
    passthrough(element, lines);
  }

  private static void ul(Element element, ArrayList<MDLine> lines) {
    list(element, lines, false);
  }

  private static void ol(Element element, ArrayList<MDLine> lines) {
    // NOTE: The way ordered lists are used makes it impossible to convert to markdown
    System.out.println("FOUND ORDERED LIST---> PASSTHROUGH!");
    passthrough(element, lines);
  }

  private static void list(Element element, ArrayList<MDLine> lines, boolean isOrdered) {
    if (element.hasAttr("class") && element.attr("class").contains("md-ignore")) {
      passthrough(element, lines);
    } else {
      indentation++;
      listDepth++;
      orderedList = isOrdered;
      for (Element child : element.children()) {
        processElement(child, lines);
      }
      listDepth--;
      indentation--;
    }
  }

  private static void li(Element element, ArrayList<MDLine> lines) {
    MDLine line;
    if (element.children().size() > 0) {
      Element child = element.child(0);
      if ("p".equals(child.tagName())) {
        child.tagName("span");
      }
    }
    line = new MDLine(orderedList ? MDLineType.Ordered : MDLineType.Unordered, indentation, "");
    lines.add(line);
    for (Node child : element.childNodes()) {
      if (child instanceof TextNode) {
        line.append(((TextNode) child).text());
      } else if (child instanceof Element) {
        processElement((Element) child, lines);
      } else {
        System.err.println("I don't know what to do with this node type in a list item: " + child);
      }
    }

  }
}
