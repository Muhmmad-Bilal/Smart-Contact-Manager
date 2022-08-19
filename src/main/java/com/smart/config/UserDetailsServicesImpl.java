package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

public class UserDetailsServicesImpl implements UserDetailsService {

	@Autowired
private	UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
    User user= userRepository.getUserByEmail(username);

if(user==null)
{
	throw new UsernameNotFoundException("Could  not Found User ");
}

CustomUserDetails customUserDetails=new CustomUserDetails(user);
return customUserDetails;


	}        

}
