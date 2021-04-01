/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.rma.util.I18n;

import java.util.ResourceBundle;

/**
 *
 * @author WatPowerUser
 */
public class FIRO_WFP_PluginI18n extends I18n{
    public static final String BUNDLE_NAME = "FIRO_WFP";
    private static final ResourceBundle SAMPLE_RESOURCE_BUNDLE;
    private ResourceBundle _resourceBundle;
    static
    { 
            SAMPLE_RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
    }
    protected FIRO_WFP_PluginI18n(String prefix, String bundleName)
    {
            super(prefix, bundleName);
    }
    public static I18n getI18n(String prefix)
    {
            return new FIRO_WFP_PluginI18n(prefix, BUNDLE_NAME);
    }
}
