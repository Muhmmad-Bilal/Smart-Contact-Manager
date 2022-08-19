package com.smart.controller;

import java.io.File;
import java.lang.StackWalker.Option;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@ModelAttribute
	public void commonMethod(Model model,Principal principal) {
		String userName=principal.getName();
		System.out.println(userName);
		User user=userRepository.getUserByEmail(userName);
		System.out.println(user);
		model.addAttribute("user", user);
	}
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title", "User DashBoard");
		return "normal/user_dashboard";
	}
	
	
	//add contact handler
	@GetMapping("/add-contact")
	
	public String  openAddContactHandler(Model model)
	{
		model.addAttribute("title", "Add contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
		 
	}
	
	//proccessing contact handler
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session)
	{
		try
		{
	String name=principal.getName();
	User user=userRepository.getUserByEmail(name);
	
	if(file.isEmpty())
	{
		System.out.println("File is Empty");
		
		//contact.setImage("contactD.png");
	}
	else
	{
		contact.setImage(file.getOriginalFilename());
		File saveFile=new ClassPathResource ("static/img").getFile();
		Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
		Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		System.out.println("File Uploaded");
		
	}
	contact.setUser(user);
	
	user.getContact().add(contact);
	this.userRepository.save(user);
	System.out.println("Save to Database");
	
	
	session.setAttribute("message", new Message("Your contact is added !! Add more", "success"));
		System.out.println("Data :"+contact);
		
	}catch(Exception e)
		{
		e.printStackTrace();
		session.setAttribute("message", new Message("Some went wrong !! try again", "danger"));
		
		}
		return "normal/add_contact_form";
	}
	
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model model,Principal principal)
	{
	String userName=	principal.getName();
	User user=	this.userRepository.getUserByEmail(userName);
    Pageable pageable=	PageRequest.of(page, 5);
	Page<Contact> contact=	this.contactRepository.findContactByUser(user.getId(),pageable);
	model.addAttribute("contacts", contact);
	model.addAttribute("currentPage", page);
	model.addAttribute("totalPages", contact.getTotalPages());
		model.addAttribute("title", "Show user Contacts");
		return "normal/show_contacts";
	}
	
	
	
	
	
	@RequestMapping("/{cId}/contact")
public String showContactDetails(@PathVariable("cId") Integer cId,Model model ,Principal principal)
{
		System.out.println("ID"+cId);
		
	Optional<Contact> contactOptional=	this.contactRepository.findById(cId);
Contact contact=	contactOptional.get();

 String userName=principal.getName();
 User user=this.userRepository.getUserByEmail(userName);
 if(user.getId()==contact.getUser().getId())
 {
	 model.addAttribute("contact", contact); 
	 model.addAttribute("title", contact.getName());
 }
 
 
 	model.addAttribute("contact", contact);
	return "normal/contact_detail";
	
}
	
	
	
	//Delete handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,HttpSession session,Principal principal)
	{
		
		Contact contact=		this.contactRepository.findById(cId).get();
		//contactOptional.get();
		
//		contact.setUser(null);
//	this.contactRepository.delete(contact);	
	
		User user=this.userRepository.getUserByEmail(principal.getName());
		user.getContact().remove(contact);
		
		this.userRepository.save(user);
	session.setAttribute("message", new Message("Contact deleted Successfully","success"));
	return "redirect:/user/show-contacts/0";
	}
	
	
	//Update Handler
	
	
	@PostMapping("/update-contact/{cId}")
	public String  updateForm(@PathVariable("cId") Integer cId,Model model)
	{
	Contact contact=	this.contactRepository.findById(cId).get();
		
		model.addAttribute("title", "Update Contact");
		model.addAttribute("contact", contact);
		
		return "normal/update_form";
		
	}
	
	//update Handler
	
	@RequestMapping(value="/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file
			,Model model,HttpSession session,Principal principal)
	{
		System.out.println(" Contact Name"+contact.getName());
		System.out.println("Contact ID"+contact.getcId());
		try
		{
			
			//this will get old contact details from the database
		Contact oldContact=	this.contactRepository.findById(contact.getcId()).get();
			
		if(!file.isEmpty())
			{
				//this code delete the old img from folder
			
			File deleteFile=new ClassPathResource ("static/img").getFile();
			File fileDelete=new File(deleteFile,oldContact.getImage());
				fileDelete.delete();
				System.out.println("Old File is delete");
				File saveFile=new ClassPathResource ("static/img").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				oldContact.setImage(oldContact.getImage());
			}
		String userName=	principal.getName();
		User user=	this.userRepository.getUserByEmail(userName);
			
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your Contact is updated", "success"));
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	
	@GetMapping("/profile")
	
	public String profile(Model model,Principal principal)
	{
		String userName=principal.getName();
	User user=	this.userRepository.getUserByEmail(userName);
		model.addAttribute("title", "Profile");
		//model.addAttribute("user", user);
		return "normal/profile";
	}
	
	
	
	
}
