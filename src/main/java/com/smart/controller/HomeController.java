package com.smart.controller;


import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
//
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@RequestMapping("/")
	public String home(Model model)
	
	{
		model.addAttribute("title", "Home-Spring Contact Manager");
		
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model)
	
	{
		model.addAttribute("title", "About-Spring Contact Manager");
		
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signUp(Model model)
	
	{
		model.addAttribute("title", "SignUp-Spring Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
//this is register User
	
	@RequestMapping(value="/do_register",method=RequestMethod.POST)
	public String registerUser(@ModelAttribute("user") User user,BindingResult bind,@RequestParam(value="agreement",defaultValue = "false") boolean agreement,Model model,HttpSession session)
	{
	try
	{
		if(!agreement)
		{
			System.out.println("You not agreed terms and condition");
			throw new Exception("You not agreed terms and condition");
		}
		if(bind.hasErrors())
		{
			System.out.println("Error"+bind.toString());
			model.addAttribute("user", user);
			return "signup";
		}
		user.setRole("ROLE_USER");
		user.setEnabled(true);
		user.setImageUrl("default.png");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		
	User result=	this.userRepository.save(user);
		
		System.out.println("Agreement"+agreement);
		System.out.println("USER:"+user);
		model.addAttribute("user",new User());
		session.setAttribute("message", new Message("Register Successfully","alert-success"));
		return "signup";
		
	}catch(Exception e)
	{
	e.printStackTrace();	
	model.addAttribute("user", user);
	session.setAttribute("message", new Message("Something went wrong"+e.getMessage(),"alert-danger"));

	return "signup";
	}
			
	}

	@GetMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title","login page");
		return "login";
	}
}
