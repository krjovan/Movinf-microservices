package com.example.api.composite.movie;

public class ServiceAddresses {
    private final String cmp;
    private final String mov;
    private final String rev;
    private final String tri;
    private final String cra;

    public ServiceAddresses() {
        cmp = null;
        mov = null;
        rev = null;
        tri = null;
        cra = null;
    }

    public ServiceAddresses(
    	String compositeAddress,
    	String movieAddress,
    	String reviewAddress,
    	String triviaAddress,
    	String crazyCreditAddress) {
    	
        this.cmp = compositeAddress;
        this.mov = movieAddress;
        this.rev = reviewAddress;
        this.tri = triviaAddress;
        this.cra = crazyCreditAddress;
    }

	public String getCmp() {
		return cmp;
	}

	public String getMov() {
		return mov;
	}

	public String getRev() {
		return rev;
	}

	public String getTri() {
		return tri;
	}

	public String getCra() {
		return cra;
	}
}
