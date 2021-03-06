package revminer.common;

import java.util.Date;

public class SearchHistory {
	private String query;
	private String friendlyName;
	private Date when;
	private final long MILLSECS_PER_DAY = 24 * 60 * 60 * 1000;
	
	public SearchHistory(String query, String friendlyName, Date when) {
		this.query = query;
		this.friendlyName = friendlyName;
		this.when = when;
	}
	
	public SearchHistory(String query, String friendlyName) {
		this(query, friendlyName, new Date());
	}
	
	public String getQuery() {
		return query;
	}
	
	// may be null
	public String getFriendlyName() {
		return friendlyName;
	}
	
	public Date getWhen() {
		return when;
	}
	
	public String serialize() {
	  StringBuilder sb = new StringBuilder();
	  sb.append(Long.toString(when.getTime()));
	  sb.append("|");
	  sb.append(query);
	  sb.append("|");
	  sb.append(friendlyName);

	  return sb.toString();
	}

	 public static SearchHistory deserialize(String s) {
	   String[] parts = s.split("\\|", 3);
	   Date date = new Date(Long.parseLong(parts[0]));
	   String query = parts[1];
	   String friendlyName;
	   if (parts.length > 2 && parts[2].length() > 0) {
		   friendlyName = parts[2];
	   } else {
		   friendlyName = null;
	   }
		   

	   return new SearchHistory(query, friendlyName, date);
	  }
	
	public String getWhenStr() {
		// this method is technically a little bit flawed for certain edge cases, but it's simple and good enough for now
		int deltaDays = (int)((new Date().getTime() - when.getTime()) / MILLSECS_PER_DAY);
		if (deltaDays == 0) {
			return "today";
		} else if (deltaDays == 1) {
			return "yesterday";
		} else if (deltaDays < 7) {
			StringBuilder sb = new StringBuilder();
			sb.append(deltaDays)
			  .append(" days");
			return sb.toString();
		} else if (deltaDays < 28) {
			int weeks = deltaDays / 7;
			StringBuilder sb = new StringBuilder();
			sb.append(weeks)
			  .append(" week");
			if (weeks > 1)
				sb.append("s");
			return sb.toString();
		} else if (deltaDays < 365) {
			int months = deltaDays / 28;
			StringBuilder sb = new StringBuilder();
			sb.append(months)
			  .append(" month");
			if (months > 1)
				sb.append("s");
			return sb.toString();
		} else {
			return "1 year+";
		}
	}

	/**
	 * @return other instanceof SearchHistory
	 *     && this.getQuery.equals(other.getQuery())
	 */
	@Override
	public boolean equals(Object other) {
	  if (!(other instanceof SearchHistory)) {
	    return false;
	  }

	  SearchHistory searchHistory = (SearchHistory)other;

	  return getQuery().equals(searchHistory.getQuery());
	}
}
