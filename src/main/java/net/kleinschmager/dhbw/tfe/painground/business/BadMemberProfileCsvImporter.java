/**
 * copyright by Robert Kleinschmager
 */
package net.kleinschmager.dhbw.tfe.painground.business;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import kr.pe.kwonnam.slf4jlambda.LambdaLogger;
import kr.pe.kwonnam.slf4jlambda.LambdaLoggerFactory;
import net.kleinschmager.dhbw.tfe.painground.persistence.model.Level;
import net.kleinschmager.dhbw.tfe.painground.persistence.model.MemberProfile;
import net.kleinschmager.dhbw.tfe.painground.persistence.model.Skill;

/**
 * Reads a file and converts this to a list of {@link MemberProfile}s
 * @author robertkleinschmager
 *
 */
@Component
public class BadMemberProfileCsvImporter implements CsvImporter {

	private static final LambdaLogger log = LambdaLoggerFactory.getLogger(BadMemberProfileCsvImporter.class);

	@Override
	public List<MemberProfile> importFile(File csvFile) {
	   
	   List<MemberProfile> listM = Lists.newArrayList();
	   SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		
		try (FileInputStream fis = new FileInputStream(csvFile)) {
			
			List<String> content = IOUtils.readLines(fis, Charset.defaultCharset());
					
			String[][] list = new String[content.size()-1][StringUtils.countMatches(content.get(0), ';')];
			
			for (int i = 1; i < content.size(); i++) {
				
			   list[i-1] = content.get(i).split(";");
			}
			
			for(int i = 0; i < list.length; i++) {
			   
			    String memberId = list[i][0];
		        String surname = list[i][1];
		        
		        if (isNotBlank(memberId) && isNotBlank(surname)) {
		        
		            MemberProfile newProfile = new MemberProfile(memberId, surname);
		            
		            newProfile.setGivenName(list[i][2]);
		            newProfile.setDateOfBirth(sdf.parse(list[i][3]));
		            
		            String[] skillsAsText = list[i][4].split(",");
		            
		            List<Skill> result = new ArrayList<>();
		            
		            for (int j=0; j < skillsAsText.length; j++)
		            {
		               Skill skill = new Skill();
		               skill.setName(skillsAsText[j]);
		               skill.setLevel(Level.NOVICE);
		               skill.setCreatedDate(new Date());
		               result.add(skill);
		            }
		            
		            result.forEach( elem -> newProfile.addSkill(elem));
		            listM.add(newProfile);
		        }
	        }
			
			return listM;
			
			
		} catch (FileNotFoundException e) {

			log.error(() -> "Could not find given file", e);
			
			return Collections.emptyList();
			
		} catch (IOException | ParseException e) {
			
			log.error(() -> "Failed to read content of given file", e);
			return Collections.emptyList();
		}
	}
	
}
